package com.instantsystem.parking.sync;

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
import static org.mockito.Mockito.when;

/**
 * End-to-end sync against the real schema (Liquibase on embedded PostgreSQL), with the Poitiers API stubbed.
 */
@SpringBootTest(properties = {"parking.sync.enabled=false", "embedded.postgres.port=0"})
class ParkingSyncIntegrationTest {

    private static final double LAT = 46.58;
    private static final double LON = 0.34;

    @MockitoBean
    private PoitiersClient poitiersClient;

    @Autowired
    private List<ParkingSyncService> syncServices;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private ParkingWriteRepository parkingWriteRepository;

    @BeforeEach
    void cleanDatabase() {
        parkingWriteRepository.deleteAll();
    }

    private static PoitiersParkingDto dto(int id, String name, int places) {
        return new PoitiersParkingDto(id, name, 320, places, 50.0, "46.5838, 0.3378", null,
                Instant.parse("2026-11-27T09:52:40Z"), null);
    }

    private void syncWith(PoitiersParkingDto... dtos) {
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(dtos.length, List.of(dtos)));
        syncServices.forEach(ParkingSyncService::syncParkings);
    }

    @Test
    void resync_updatesExistingParkingsInsteadOfDuplicating() {
        syncWith(dto(3, "THEATRE", 50), dto(1, "NOTRE DAME", 58));
        syncWith(dto(3, "THEATRE", 17), dto(1, "NOTRE DAME", 60));

        List<ParkingDto> parkings = parkingService.getParkings(LAT, LON, 5);

        assertThat(parkingWriteRepository.count()).isEqualTo(2);
        assertThat(parkings).extracting(ParkingDto::name, ParkingDto::availableSpaces)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("THEATRE", 17),
                        org.assertj.core.groups.Tuple.tuple("NOTRE DAME", 60));
    }

    @Test
    void resync_parkingRemovedFromSource_isNoLongerServed() {
        syncWith(dto(3, "THEATRE", 50), dto(1, "NOTRE DAME", 58));
        syncWith(dto(3, "THEATRE", 17));

        assertThat(parkingService.getParkings(LAT, LON, 5))
                .extracting(ParkingDto::name)
                .containsExactly("THEATRE");
    }

    @Test
    void resync_emptyPayload_keepsLastKnownData() {
        syncWith(dto(3, "THEATRE", 50));
        syncWith();

        assertThat(parkingService.getParkings(LAT, LON, 5))
                .extracting(ParkingDto::name)
                .containsExactly("THEATRE");
    }
}
