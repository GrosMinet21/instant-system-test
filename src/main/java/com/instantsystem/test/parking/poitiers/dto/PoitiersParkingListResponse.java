package com.instantsystem.test.parking.poitiers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Réponse de l'API parkings de Poitiers : un total et la liste des résultats.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PoitiersParkingListResponse(
        @JsonProperty("total") Integer total,
        @JsonProperty("results") List<PoitiersParkingDto> results
) {
}
