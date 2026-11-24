package com.instantsystem.test.parking.poitiers.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PoitiersParkingDtoDeserializationTest {

    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void deserializes_full_payload() {
        String json = """
                {"total":9,"results":[
                  {"infos_parkingsgeo_point":"46.58383455409422, 0.33779491061805567","_rand":424281,
                   "_geopoint":"46.58383455409422, 0.33779491061805567","Dernière_mise_à_jour_Base":"2026-11-24T16:35:36.000Z",
                   "Capacite":320,"_i":2730275881012141,"Id":3,"Nom":"THEATRE","_updatedAt":"2026-11-24T16:29:24.206Z",
                   "taux_doccupation":84.375,"Places":50,"_score":null,"_id":"xEtNIKJ6L7QKGMa2MLRhZ"},
                  {"_rand":555117,"Dernière_mise_à_jour_Base":"2026-11-24T16:35:34.000Z","Capacite":480,
                   "_i":2730275881012139,"Id":5,"Nom":"GARE EFFIA","_updatedAt":"2026-11-24T16:29:24.206Z",
                   "taux_doccupation":51.45833333333334,"Places":233,"_score":null,"_id":"bFgpNnlogjSFS7NQv3e33"}
                ]}
                """;

        PoitiersParkingListResponse response = mapper.readValue(json, PoitiersParkingListResponse.class);

        assertEquals(9, response.total());
        assertEquals(2, response.results().size());

        PoitiersParkingDto theatre = response.results().get(0);
        assertEquals(3, theatre.id());
        assertEquals("THEATRE", theatre.nom());
        assertEquals(320, theatre.capacite());
        assertEquals(50, theatre.places());
        assertEquals(84.375, theatre.tauxDoccupation());
        assertEquals("46.58383455409422, 0.33779491061805567", theatre.geopoint());
        assertEquals(2730275881012141L, theatre.internalIndex());
        assertEquals(Instant.parse("2026-11-24T16:35:36.000Z"), theatre.derniereMiseAJourBase());
        assertEquals(Instant.parse("2026-11-24T16:29:24.206Z"), theatre.updatedAt());
        assertEquals("xEtNIKJ6L7QKGMa2MLRhZ", theatre.documentId());
        assertNull(theatre.score());

        // Champs géo absents sur certains parkings -> null, pas d'erreur
        PoitiersParkingDto gareEffia = response.results().get(1);
        assertEquals("GARE EFFIA", gareEffia.nom());
        assertNull(gareEffia.geopoint());
        assertNull(gareEffia.infosParkingsGeoPoint());
    }
}
