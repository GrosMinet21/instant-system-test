package com.instantsystem.test.parking.poitiers.cron;

import com.instantsystem.test.parking.poitiers.service.PoitiersParkingsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Synchronise la base avec l'API temps réel des parkings de Poitiers :
 * au démarrage de l'application puis toutes les 5 minutes (un {@code fixedRate}
 * déclenche la première exécution dès le contexte prêt).
 */
@Component
public class PoitiersParkingSyncCron {

    private final PoitiersParkingsService poitiersParkingsService;

    public PoitiersParkingSyncCron(PoitiersParkingsService poitiersParkingsService) {
        this.poitiersParkingsService = poitiersParkingsService;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void syncParkings() {
        poitiersParkingsService.syncParkings();
    }
}
