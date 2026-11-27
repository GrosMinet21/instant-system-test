package com.instantsystem.parking.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables the periodic parking synchronisation. Can be turned off with {@code parking.sync.enabled=false}
 * (e.g. in tests, so that they never call the real data sources).
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "parking.sync.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
