package com.instantsystem.parking.cron;

import com.instantsystem.parking.sync.ParkingSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
public class ParkingSyncCron{

    private final Collection<ParkingSyncService> parkingSyncServices;

    public ParkingSyncCron(Collection<ParkingSyncService> parkingSyncServices) {
        this.parkingSyncServices = parkingSyncServices;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void syncParkings() {
        parkingSyncServices.forEach(ParkingSyncService::syncParkings);
    }
}
