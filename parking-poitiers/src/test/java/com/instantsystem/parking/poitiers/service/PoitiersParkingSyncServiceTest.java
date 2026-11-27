package com.instantsystem.parking.poitiers.service;

import com.instantsystem.parking.poitiers.PoitiersClient;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoitiersParkingSyncServiceTest {

    @Mock
    private PoitiersClient poitiersClient;
    @Mock
    private PoitiersParkingWriter poitiersParkingWriter;

    @InjectMocks
    private PoitiersParkingSyncService service;

    @Test
    void syncParkings_delegatesPayloadToWriter() {
        List<PoitiersParkingDto> results = List.of(
                new PoitiersParkingDto(3, "THEATRE", 320, 50, 84.3, "46.5, 0.3", null, null, null));
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(1, results));

        service.syncParkings();

        verify(poitiersParkingWriter).saveParkings(results);
    }

    @Test
    void syncParkings_nullResponse_keepsLastKnownData() {
        when(poitiersClient.getParkings()).thenReturn(null);

        service.syncParkings();

        verify(poitiersParkingWriter, never()).saveParkings(any());
    }

    @Test
    void syncParkings_nullResults_keepsLastKnownData() {
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(0, null));

        service.syncParkings();

        verify(poitiersParkingWriter, never()).saveParkings(any());
    }

    @Test
    void syncParkings_emptyResults_keepsLastKnownData() {
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(0, List.of()));

        service.syncParkings();

        verify(poitiersParkingWriter, never()).saveParkings(any());
    }
}
