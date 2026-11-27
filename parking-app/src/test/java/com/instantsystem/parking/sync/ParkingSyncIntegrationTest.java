package com.instantsystem.parking.sync;

import com.instantsystem.parking.cron.ParkingSyncCron;
import com.instantsystem.parking.database.repository.ParkingWriteRepository;
import com.instantsystem.parking.dto.ParkingDto;
import com.instantsystem.parking.poitiers.PoitiersClient;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import com.instantsystem.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * End-to-end sync (cron → Poitiers source → generic writer) against the real schema (Liquibase on embedded
 * PostgreSQL), with the Poitiers API stubbed.
 */
@SpringBootTest(properties = {"parking.sync.enabled=false", "embedded.postgres.port=0"})
class ParkingSyncIntegrationTest {

    private static final double LAT = 46.58;
    private static final double LON = 0.34;
    private static final Instant UPDATED_AT = Instant.parse("2026-11-27T10:43:46.128Z");

    @MockitoBean
    private PoitiersClient poitiersClient;

    @Autowired
    private ParkingSyncCron parkingSyncCron;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private ParkingWriteRepository parkingWriteRepository;

    @BeforeEach
    void cleanDatabase() {
        parkingWriteRepository.deleteAll();
    }

    private static PoitiersParkingDto dto(Integer id, String name, int places) {
        return new PoitiersParkingDto(id, name, 320, places, 50.0, "46.5838, 0.3378", null, UPDATED_AT);
    }

    private void syncWith(Integer total, PoitiersParkingDto... dtos) {
        when(poitiersClient.getParkings(anyInt()))
                .thenReturn(new PoitiersParkingListResponse(total, null, List.of(dtos)));
        parkingSyncCron.syncParkings();
    }

    private void syncWith(PoitiersParkingDto... dtos) {
        syncWith(dtos.length, dtos);
    }

    private List<ParkingDto> nearbyParkings() {
        return parkingService.getParkings(LAT, LON, 5);
    }

    @Test
    void resync_updatesExistingParkingsInsteadOfDuplicating() {
        syncWith(dto(3, "THEATRE", 50), dto(1, "NOTRE DAME", 58));
        syncWith(dto(3, "THEATRE", 17), dto(1, "NOTRE DAME", 60));

        assertThat(parkingWriteRepository.count()).isEqualTo(2);
        assertThat(nearbyParkings()).extracting(ParkingDto::name, ParkingDto::availableSpaces, ParkingDto::lastUpdated)
                .containsExactlyInAnyOrder(
                        tuple("THEATRE", 17, UPDATED_AT),
                        tuple("NOTRE DAME", 60, UPDATED_AT));
    }

    @Test
    void resync_parkingRemovedFromSource_isNoLongerServed() {
        syncWith(dto(3, "THEATRE", 50), dto(1, "NOTRE DAME", 58));
        syncWith(dto(3, "THEATRE", 17));

        assertThat(nearbyParkings()).extracting(ParkingDto::name).containsExactly("THEATRE");
    }

    @Test
    void resync_emptyPayload_keepsLastKnownData() {
        syncWith(dto(3, "THEATRE", 50));
        syncWith();

        assertThat(nearbyParkings()).extracting(ParkingDto::name).containsExactly("THEATRE");
    }

    @Test
    void resync_sourceFormatChanged_keepsLastKnownData() {
        syncWith(dto(3, "THEATRE", 50), dto(1, "NOTRE DAME", 58));
        // e.g. "Id" and "Nom" renamed in the source: every line deserializes without them.
        syncWith(dto(null, null, 0), dto(null, null, 0));

        assertThat(nearbyParkings()).extracting(ParkingDto::name, ParkingDto::availableSpaces)
                .containsExactlyInAnyOrder(tuple("THEATRE", 50), tuple("NOTRE DAME", 58));
    }

    @Test
    void resync_incompletePayload_keepsLastKnownData() {
        syncWith(dto(3, "THEATRE", 50), dto(1, "NOTRE DAME", 58));
        // 3 lines announced but only 1 returned and no next page: the missing parkings must not be deleted.
        syncWith(3, dto(3, "THEATRE", 17));

        assertThat(nearbyParkings()).extracting(ParkingDto::name, ParkingDto::availableSpaces)
                .containsExactlyInAnyOrder(tuple("THEATRE", 50), tuple("NOTRE DAME", 58));
    }
}
