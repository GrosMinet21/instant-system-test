package com.instantsystem.parking.cron;

import com.instantsystem.parking.sync.ParkingSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
public class ParkingSyncCron {

    private static final Logger log = LoggerFactory.getLogger(ParkingSyncCron.class);

    private final Collection<ParkingSyncService> parkingSyncServices;

    public ParkingSyncCron(Collection<ParkingSyncService> parkingSyncServices) {
        this.parkingSyncServices = parkingSyncServices;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void syncParkings() {
        for (ParkingSyncService service : parkingSyncServices) {
            try {
                service.syncParkings();
            } catch (RuntimeException e) {
                log.error("Parking sync failed for {}, keeping last known data",
                        AopUtils.getTargetClass(service).getSimpleName(), e);
            }
        }
    }
}
