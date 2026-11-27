package com.instantsystem.parking.sync;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import com.instantsystem.parking.database.repository.CityRepository;
import com.instantsystem.parking.database.repository.ParkingReadRepository;
import com.instantsystem.parking.database.repository.ParkingWriteRepository;
import com.instantsystem.parking.dto.CityName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSnapshotWriterTest {

    private static final CityName CITY = CityName.POITIERS;

    @Mock
    private CityRepository cityRepository;
    @Mock
    private ParkingWriteRepository parkingWriteRepository;
    @Mock
    private ParkingReadRepository parkingReadRepository;

    @InjectMocks
    private ParkingSnapshotWriter writer;

    @Captor
    private ArgumentCaptor<Collection<Parking>> savedCaptor;
    @Captor
    private ArgumentCaptor<Collection<Parking>> deletedCaptor;

    private static ParkingSnapshot snapshot(String externalId, String name, int places, Double lat, Double lon,
                                            Instant lastUpdated) {
        return new ParkingSnapshot(externalId, name, 320, places, 84.3, lat, lon, lastUpdated);
    }

    private static ParkingSnapshot snapshot(String externalId, String name) {
        return snapshot(externalId, name, 50, 46.5, 0.3, null);
    }

    private static City city() {
        City city = new City();
        city.setId(1L);
        city.setName(CITY);
        return city;
    }

    private static Parking existing(long id, String externalId, City city) {
        Parking parking = new Parking();
        parking.setId(id);
        parking.setExternalId(externalId);
        parking.setName("EXISTING " + externalId);
        parking.setAvailableSpaces(99);
        parking.setCity(city);
        return parking;
    }

    private City givenCityWith(Parking... parkings) {
        City city = parkings.length > 0 ? parkings[0].getCity() : city();
        when(cityRepository.findByName(CITY)).thenReturn(Optional.of(city));
        when(parkingReadRepository.findByCity(city)).thenReturn(List.of(parkings));
        return city;
    }

    private List<Parking> saved() {
        verify(parkingWriteRepository).saveAll(savedCaptor.capture());
        return new ArrayList<>(savedCaptor.getValue());
    }

    private List<Parking> deleted() {
        verify(parkingWriteRepository).deleteAll(deletedCaptor.capture());
        return new ArrayList<>(deletedCaptor.getValue());
    }

    private void verifyNothingWritten() {
        verify(parkingWriteRepository, never()).saveAll(any());
        verify(parkingWriteRepository, never()).deleteAll(any());
    }

    @Test
    void write_createsCityWhenAbsent_andInsertsNewParking() {
        when(cityRepository.findByName(CITY)).thenReturn(Optional.empty());
        City savedCity = city();
        when(cityRepository.save(any(City.class))).thenReturn(savedCity);

        writer.write(CITY, List.of(snapshot("3", "THEATRE")));

        verify(cityRepository).save(any(City.class));
        List<Parking> saved = saved();
        assertThat(saved).hasSize(1);
        Parking parking = saved.get(0);
        assertThat(parking.getId()).isNull();
        assertThat(parking.getExternalId()).isEqualTo("3");
        assertThat(parking.getName()).isEqualTo("THEATRE");
        assertThat(parking.getCity()).isEqualTo(savedCity);
        assertThat(deleted()).isEmpty();
    }

    @Test
    void write_updatesExistingParkingMatchedOnBusinessId() {
        Parking theatre = existing(42L, "3", city());
        givenCityWith(theatre);

        writer.write(CITY, List.of(snapshot("3", "THEATRE", 17, 46.5, 0.3, null)));

        assertThat(saved()).containsExactly(theatre);
        assertThat(theatre.getId()).isEqualTo(42L);
        assertThat(theatre.getAvailableSpaces()).isEqualTo(17);
        assertThat(deleted()).isEmpty();
    }

    @Test
    void write_deletesParkingsNoLongerListed() {
        City city = city();
        Parking theatre = existing(42L, "3", city);
        Parking closed = existing(43L, "99", city);
        givenCityWith(theatre, closed);

        writer.write(CITY, List.of(snapshot("3", "THEATRE")));

        assertThat(saved()).containsExactly(theatre);
        assertThat(deleted()).containsExactly(closed);
    }

    @Test
    void write_invalidRecords_areSkippedWithoutAbortingTheSync() {
        givenCityWith();

        writer.write(CITY, List.of(
                snapshot(null, "NO ID"),
                snapshot(" ", "BLANK ID"),
                snapshot("4", null),
                snapshot("3", "THEATRE"),
                snapshot("3", "THEATRE DUPLICATE")));

        assertThat(saved()).extracting(Parking::getName).containsExactly("THEATRE");
    }

    @Test
    void write_listedParkingWithInvalidRecord_keepsItsLastKnownData() {
        City city = city();
        Parking theatre = existing(42L, "3", city);
        Parking notreDame = existing(43L, "1", city);
        givenCityWith(theatre, notreDame);

        // Notre Dame is still listed, but without a name this time.
        writer.write(CITY, List.of(snapshot("3", "THEATRE"), snapshot("1", null)));

        assertThat(saved()).containsExactly(theatre);
        assertThat(deleted()).isEmpty();
        assertThat(notreDame.getName()).isEqualTo("EXISTING 1");
        assertThat(notreDame.getAvailableSpaces()).isEqualTo(99);
    }

    @Test
    void write_noValidRecord_keepsLastKnownData() {
        givenCityWith(existing(42L, "3", city()));

        // e.g. the source renamed its fields: every record is invalid.
        writer.write(CITY, List.of(snapshot(null, null), snapshot(null, null)));

        verifyNothingWritten();
    }

    @Test
    void write_invalidOrMissingCoordinates_areDroppedButParkingIsKept() {
        givenCityWith();

        writer.write(CITY, List.of(
                snapshot("1", "OUT OF RANGE", 10, 95.0, 0.3, null),
                snapshot("2", "NAN", 10, Double.NaN, 0.3, null),
                snapshot("3", "LATITUDE ONLY", 10, 46.5, null, null),
                snapshot("4", "VALID", 10, 46.5, 0.3, null)));

        List<Parking> saved = saved();
        assertThat(saved).extracting(Parking::getName)
                .containsExactly("OUT OF RANGE", "NAN", "LATITUDE ONLY", "VALID");
        assertThat(saved.subList(0, 3)).allSatisfy(parking -> {
            assertThat(parking.getLatitude()).isNull();
            assertThat(parking.getLongitude()).isNull();
        });
        assertThat(saved.get(3).getLatitude()).isEqualTo(46.5);
        assertThat(saved.get(3).getLongitude()).isEqualTo(0.3);
    }

    @Test
    void write_lastUpdatedInTheFuture_isCappedToSyncTime() {
        givenCityWith();
        Instant past = Instant.parse("2026-01-01T10:00:00Z");
        Instant future = Instant.now().plus(1, ChronoUnit.HOURS);

        Instant before = Instant.now();
        writer.write(CITY, List.of(
                snapshot("1", "PAST", 10, 46.5, 0.3, past),
                snapshot("2", "FUTURE", 10, 46.5, 0.3, future)));
        Instant after = Instant.now();

        List<Parking> saved = saved();
        assertThat(saved.get(0).getLastUpdated()).isEqualTo(past);
        assertThat(saved.get(1).getLastUpdated()).isBetween(before, after);
    }
}
