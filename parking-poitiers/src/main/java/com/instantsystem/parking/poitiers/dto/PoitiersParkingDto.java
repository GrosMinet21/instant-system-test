package com.instantsystem.parking.poitiers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * A parking line of the Poitiers data-fair dataset. Only the fields used by the sync are mapped; the data-fair
 * technical fields ({@code _id}, {@code _i}, {@code _rand}, {@code _score}) are ignored — in particular
 * {@code _id} is regenerated on every dataset refresh and cannot identify a parking. "Dernière_mise_à_jour_Base" is
 * ignored too: it was observed several minutes in the future; {@code _updatedAt} is used instead.
 */
public record PoitiersParkingDto(
        @JsonProperty("Id") Integer id,
        @JsonProperty("Nom") String name,
        @JsonProperty("Capacite") Integer capacity,
        @JsonProperty("Places") Integer availableSpaces,
        @JsonProperty("taux_doccupation") Double occupancyRate,
        @JsonProperty("_geopoint") String geopoint,
        @JsonProperty("infos_parkingsgeo_point") String altGeoPoint,
        @JsonProperty("_updatedAt") Instant updatedAt
) {
}
