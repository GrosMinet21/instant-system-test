package com.instantsystem.test.parking.dto;

import java.time.Instant;

/**
 * Représentation d'API d'un parking (exposée par le contrôleur).
 * La ville est aplatie sur {@code cityName}.
 */
public record ParkingDto(
        Long id,
        String name,
        Integer capacity,
        Integer availableSpaces,
        Double occupancyRate,
        Double latitude,
        Double longitude,
        Instant lastUpdatedBase,
        Instant sourceUpdatedAt,
        String cityName
) {
}
