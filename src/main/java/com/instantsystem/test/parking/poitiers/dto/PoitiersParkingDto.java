package com.instantsystem.test.parking.poitiers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Un parking tel que renvoyé par l'API de Poitiers.
 *
 * <p>Les champs métier (id, nom, capacité, places, taux d'occupation, géolocalisation)
 * sont exposés sous des noms Java idiomatiques ; les champs techniques de l'API
 * (préfixés par {@code _}) sont conservés mais peuvent être ignorés par les consommateurs.</p>
 *
 * <p>{@code infos_parkingsgeo_point} / {@code _geopoint} sont au format
 * {@code "latitude, longitude"} et sont parfois absents de la réponse.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
