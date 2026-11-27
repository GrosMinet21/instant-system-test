package com.instantsystem.parking.poitiers.service;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import com.instantsystem.parking.database.repository.CityRepository;
import com.instantsystem.parking.database.repository.ParkingReadRepository;
import com.instantsystem.parking.database.repository.ParkingWriteRepository;
import com.instantsystem.parking.dto.CityName;
import com.instantsystem.parking.poitiers.PoitiersClient;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

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
class PoitiersParkingSyncServiceTest {

    @Mock
    private PoitiersClient poitiersClient;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private ParkingWriteRepository parkingRepository;
    @Mock
    private ParkingReadRepository parkingReadRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private PoitiersParkingSyncService service;

    @Captor
    private ArgumentCaptor<Collection<Parking>> savedCaptor;
    @Captor
    private ArgumentCaptor<Collection<Parking>> deletedCaptor;

    private static PoitiersParkingDto dto(Integer id, String nom, int places, String geopoint) {
        return new PoitiersParkingDto(id, nom, 320, places, 84.3, geopoint, null, null, null);
    }

    private static PoitiersParkingDto dto(Integer id, String nom) {
        return dto(id, nom, 50, "46.5, 0.3");
    }

    private static City poitiersCity() {
        City city = new City();
        city.setId(1L);
        city.setName(CityName.POITIERS);
        return city;
    }

    private static Parking existing(long id, String externalId, City city) {
        Parking parking = new Parking();
        parking.setId(id);
        parking.setExternalId(externalId);
        parking.setCity(city);
        return parking;
    }

    private void givenPayload(PoitiersParkingDto... dtos) {
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(dtos.length, List.of(dtos)));
    }

    private List<Parking> saved() {
        verify(parkingRepository).saveAll(savedCaptor.capture());
        return new ArrayList<>(savedCaptor.getValue());
    }

    private List<Parking> deleted() {
        verify(parkingRepository).deleteAll(deletedCaptor.capture());
        return new ArrayList<>(deletedCaptor.getValue());
    }

    @Test
    void syncParkings_nullResponse_doesNothing() {
        when(poitiersClient.getParkings()).thenReturn(null);

        service.syncParkings();

        verify(parkingRepository, never()).saveAll(any());
        verify(parkingRepository, never()).deleteAll(any());
    }

    @Test
    void syncParkings_nullResults_doesNothing() {
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(0, null));

        service.syncParkings();

        verify(parkingRepository, never()).saveAll(any());
        verify(parkingRepository, never()).deleteAll(any());
    }

    @Test
    void syncParkings_emptyResults_keepsLastKnownData() {
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(0, List.of()));

        service.syncParkings();

        verify(parkingRepository, never()).saveAll(any());
        verify(parkingRepository, never()).deleteAll(any());
    }

    @Test
    void syncParkings_createsCityWhenAbsent_andInsertsNewParking() {
        givenPayload(dto(3, "THEATRE"));
        when(cityRepository.findByName(CityName.POITIERS)).thenReturn(Optional.empty());
        City savedCity = poitiersCity();
        when(cityRepository.save(any(City.class))).thenReturn(savedCity);

        service.syncParkings();

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
    void syncParkings_matchesOnBusinessId_evenWhenDocumentIdIsRegenerated() {
        City city = poitiersCity();
        Parking theatre = existing(42L, "3", city);
        when(cityRepository.findByName(CityName.POITIERS)).thenReturn(Optional.of(city));
        when(parkingReadRepository.findByCity(city)).thenReturn(List.of(theatre));
        // Same parking (Id 3), new data-fair _id and new availability.
        givenPayload(dto(3, "THEATRE", 17, "46.5, 0.3"));

        service.syncParkings();

        List<Parking> saved = saved();
        assertThat(saved).containsExactly(theatre);
        assertThat(theatre.getId()).isEqualTo(42L);
        assertThat(theatre.getAvailableSpaces()).isEqualTo(17);
        assertThat(deleted()).isEmpty();
    }

    @Test
    void syncParkings_deletesParkingsNoLongerInSource() {
        City city = poitiersCity();
        Parking theatre = existing(42L, "3", city);
        Parking closed = existing(43L, "99", city);
        when(cityRepository.findByName(CityName.POITIERS)).thenReturn(Optional.of(city));
        when(parkingReadRepository.findByCity(city)).thenReturn(List.of(theatre, closed));
        givenPayload(dto(3, "THEATRE"));

        service.syncParkings();

        assertThat(saved()).containsExactly(theatre);
        assertThat(deleted()).containsExactly(closed);
    }

    @Test
    void syncParkings_invalidRecords_areSkippedWithoutAbortingTheSync() {
        when(cityRepository.findByName(CityName.POITIERS)).thenReturn(Optional.of(poitiersCity()));
        givenPayload(
                dto(null, "NO ID"),
                dto(4, null),
                dto(3, "THEATRE"),
                dto(3, "THEATRE DUPLICATE"),
                dto(5, "BAD GEO", 10, "not, a-number"));

        service.syncParkings();

        List<Parking> saved = saved();
        assertThat(saved).extracting(Parking::getName).containsExactly("THEATRE", "BAD GEO");
        Parking badGeo = saved.get(1);
        assertThat(badGeo.getLatitude()).isNull();
        assertThat(badGeo.getLongitude()).isNull();
        assertThat(badGeo.getAvailableSpaces()).isEqualTo(10);
    }
}
