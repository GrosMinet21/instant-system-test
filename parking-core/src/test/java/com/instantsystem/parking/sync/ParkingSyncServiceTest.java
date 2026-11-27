package com.instantsystem.parking.sync;

import com.instantsystem.parking.dto.CityName;
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
class ParkingSyncServiceTest {

    @Mock
    private ParkingSnapshotWriter parkingSnapshotWriter;
    @Mock
    private ParkingSource source;

    @InjectMocks
    private ParkingSyncService service;

    @Test
    void sync_writesFetchedSnapshotsForTheSourceCity() {
        List<ParkingSnapshot> snapshots = List.of(
                new ParkingSnapshot("3", "THEATRE", 320, 50, 84.3, 46.5, 0.3, null));
        when(source.city()).thenReturn(CityName.POITIERS);
        when(source.fetchParkings()).thenReturn(snapshots);

        service.sync(source);

        verify(parkingSnapshotWriter).write(CityName.POITIERS, snapshots);
    }

    @Test
    void sync_emptyPayload_keepsLastKnownData() {
        when(source.fetchParkings()).thenReturn(List.of());

        service.sync(source);

        verify(parkingSnapshotWriter, never()).write(any(), any());
    }

    @Test
    void sync_nullPayload_keepsLastKnownData() {
        when(source.fetchParkings()).thenReturn(null);

        service.sync(source);

        verify(parkingSnapshotWriter, never()).write(any(), any());
    }
}
