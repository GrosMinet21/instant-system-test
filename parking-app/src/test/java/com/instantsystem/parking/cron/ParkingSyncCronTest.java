package com.instantsystem.parking.cron;

import com.instantsystem.parking.sync.ParkingSyncService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ParkingSyncCronTest {

    @Test
    void syncParkings_callsEachService() {
        ParkingSyncService serviceA = mock(ParkingSyncService.class);
        ParkingSyncService serviceB = mock(ParkingSyncService.class);
        ParkingSyncCron cron = new ParkingSyncCron(List.of(serviceA, serviceB));

        cron.syncParkings();

        verify(serviceA).syncParkings();
        verify(serviceB).syncParkings();
    }

    @Test
    void syncParkings_failingService_doesNotPreventOthers() {
        ParkingSyncService failing = mock(ParkingSyncService.class);
        ParkingSyncService healthy = mock(ParkingSyncService.class);
        doThrow(new IllegalStateException("source down")).when(failing).syncParkings();
        ParkingSyncCron cron = new ParkingSyncCron(List.of(failing, healthy));

        assertThatCode(cron::syncParkings).doesNotThrowAnyException();

        verify(healthy).syncParkings();
    }
}
