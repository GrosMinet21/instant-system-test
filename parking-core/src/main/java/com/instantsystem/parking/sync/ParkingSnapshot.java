package com.instantsystem.parking.sync;

import java.time.Instant;

/**
 * A parking as provided by a {@link ParkingSource}, in a source-neutral format. Each city adapter maps its own source
 * format onto this record; validation and persistence are then handled generically by the synchronisation.
 *
 * @param externalId      stable business key of the parking in its source, unique per city (required)
 * @param name            display name (required)
 * @param capacity        total number of spaces, if known
 * @param availableSpaces number of free spaces, if known
 * @param occupancyRate   occupancy rate in percent (0-100), if known
 * @param latitude        WGS84 latitude, {@code null} if unknown
 * @param longitude       WGS84 longitude, {@code null} if unknown
 * @param lastUpdated     when the source last updated the availability, if known
 */
public record ParkingSnapshot(
        String externalId,
        String name,
        Integer capacity,
        Integer availableSpaces,
        Double occupancyRate,
        Double latitude,
        Double longitude,
        Instant lastUpdated
) {
}
