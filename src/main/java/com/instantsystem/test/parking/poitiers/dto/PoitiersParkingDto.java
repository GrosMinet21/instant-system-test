package com.instantsystem.test.parking.poitiers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record PoitiersParkingDto(
        @JsonProperty("Id") Integer id,
        @JsonProperty("Nom") String nom,
        @JsonProperty("Capacite") Integer capacite,
        @JsonProperty("Places") Integer places,
        @JsonProperty("taux_doccupation") Double tauxDoccupation,
        @JsonProperty("_geopoint") String geopoint,
        @JsonProperty("infos_parkingsgeo_point") String infosParkingsGeoPoint,
        @JsonProperty("Dernière_mise_à_jour_Base") Instant derniereMiseAJourBase,
        @JsonProperty("_updatedAt") Instant updatedAt,
        @JsonProperty("_id") String documentId,
        @JsonProperty("_i") Long internalIndex,
        @JsonProperty("_rand") Integer rand,
        @JsonProperty("_score") Double score
) {
}
