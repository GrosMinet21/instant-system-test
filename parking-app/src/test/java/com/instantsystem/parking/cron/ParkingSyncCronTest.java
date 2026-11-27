package com.instantsystem.parking.cron;

import com.instantsystem.parking.dto.CityName;
import com.instantsystem.parking.sync.ParkingSource;
import com.instantsystem.parking.sync.ParkingSyncService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParkingSyncCronTest {

    private final ParkingSyncService parkingSyncService = mock(ParkingSyncService.class);

    @Test
    void syncParkings_syncsEachSource() {
        ParkingSource sourceA = mock(ParkingSource.class);
        ParkingSource sourceB = mock(ParkingSource.class);
        ParkingSyncCron cron = new ParkingSyncCron(List.of(sourceA, sourceB), parkingSyncService);

        cron.syncParkings();

        verify(parkingSyncService).sync(sourceA);
        verify(parkingSyncService).sync(sourceB);
    }

    @Test
    void syncParkings_failingSource_doesNotPreventOthers() {
        ParkingSource failing = mock(ParkingSource.class);
        ParkingSource healthy = mock(ParkingSource.class);
        when(failing.city()).thenReturn(CityName.POITIERS);
        doThrow(new IllegalStateException("source down")).when(parkingSyncService).sync(failing);
        ParkingSyncCron cron = new ParkingSyncCron(List.of(failing, healthy), parkingSyncService);

        assertThatCode(cron::syncParkings).doesNotThrowAnyException();

        verify(parkingSyncService).sync(healthy);
    }
}
