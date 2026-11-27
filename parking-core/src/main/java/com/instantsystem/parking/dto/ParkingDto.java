package com.instantsystem.parking.dto;

import java.time.Instant;

public record ParkingDto(
        String name,
        Integer capacity,
        Integer availableSpaces,
        Double occupancyRate,
        Double latitude,
        Double longitude,
        String city,
        Double distanceMeters,
        Instant lastUpdated
) {
}
