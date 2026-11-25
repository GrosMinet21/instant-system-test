package com.instantsystem.test.parking.cron;

import com.instantsystem.test.parking.cron.ParkingSyncCron;
import com.instantsystem.test.parking.poitiers.service.PoitiersParkingsService;
import com.instantsystem.test.parking.sync.ParkingSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParkingSyncCronTest {

    @Mock
    private PoitiersParkingsService poitiersParkingsService;

    @Test
    void syncParkings_delegatesToAllServices() {
        ParkingSyncCron cron = new ParkingSyncCron(List.of(poitiersParkingsService));

        cron.syncParkings();

        verify(poitiersParkingsService).syncParkings();
    }

    @Test
    void syncParkings_callsEachService() {
        ParkingSyncService serviceA = org.mockito.Mockito.mock(ParkingSyncService.class);
        ParkingSyncService serviceB = org.mockito.Mockito.mock(ParkingSyncService.class);
        ParkingSyncCron cron = new ParkingSyncCron(List.of(serviceA, serviceB));

        cron.syncParkings();

        verify(serviceA).syncParkings();
        verify(serviceB).syncParkings();
    }
}
