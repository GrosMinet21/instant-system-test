package com.instantsystem.parking.poitiers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record PoitiersParkingDto(
        @JsonProperty("Id") Integer id,
        @JsonProperty("Nom") String name,
        @JsonProperty("Capacite") Integer capacity,
        @JsonProperty("Places") Integer availableSpaces,
        @JsonProperty("taux_doccupation") Double occupancyRate,
        @JsonProperty("_geopoint") String geopoint,
        @JsonProperty("infos_parkingsgeo_point") String altGeoPoint,
        @JsonProperty("Dernière_mise_à_jour_Base") Instant lastUpdatedBase,
        @JsonProperty("_updatedAt") Instant updatedAt,
        @JsonProperty("_id") String documentId,
        @JsonProperty("_i") Long internalIndex,
        @JsonProperty("_rand") Integer rand,
        @JsonProperty("_score") Double score
) {
}
