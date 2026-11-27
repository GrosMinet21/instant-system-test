package com.instantsystem.parking.dto;

import java.time.Instant;

/**
 * A parking as exposed by the REST API. This is the public contract for mobile/web clients: it must stay
 * independent from any city-specific source format.
 *
 * @param city        city name, as a plain string so that adding a city never changes the API schema
 * @param lastUpdated when the source last measured the availability, so that clients can detect stale data
 */
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
