package com.instantsystem.parking.cron;

import com.instantsystem.parking.sync.ParkingSource;
import com.instantsystem.parking.sync.ParkingSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ParkingSyncCron {

    private static final Logger log = LoggerFactory.getLogger(ParkingSyncCron.class);

    private final List<ParkingSource> parkingSources;
    private final ParkingSyncService parkingSyncService;

    public ParkingSyncCron(List<ParkingSource> parkingSources, ParkingSyncService parkingSyncService) {
        this.parkingSources = parkingSources;
        this.parkingSyncService = parkingSyncService;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void syncParkings() {
        for (ParkingSource source : parkingSources) {
            try {
                parkingSyncService.sync(source);
            } catch (RuntimeException e) {
                log.error("Parking sync failed for {}, keeping last known data", source.city(), e);
            }
        }
    }
}
