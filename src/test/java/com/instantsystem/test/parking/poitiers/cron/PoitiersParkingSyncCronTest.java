package com.instantsystem.test.parking.poitiers.cron;

import com.instantsystem.test.parking.poitiers.service.PoitiersParkingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PoitiersParkingSyncCronTest {

    @Mock
    private PoitiersParkingsService poitiersParkingsService;

    @InjectMocks
    private PoitiersParkingSyncCron cron;

    @Test
    void syncParkings_delegatesToService() {
        cron.syncParkings();

        verify(poitiersParkingsService).syncParkings();
    }
}
