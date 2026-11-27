package com.instantsystem.parking.cron;

import com.instantsystem.parking.poitiers.service.PoitiersParkingSyncService;
import com.instantsystem.parking.sync.ParkingSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ParkingSyncCronTest {

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
