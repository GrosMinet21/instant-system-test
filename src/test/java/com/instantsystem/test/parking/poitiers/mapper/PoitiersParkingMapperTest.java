package com.instantsystem.test.parking.poitiers.mapper;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PoitiersParkingMapperTest {

    private final PoitiersParkingMapper mapper = Mappers.getMapper(PoitiersParkingMapper.class);

    private PoitiersParkingDto dto(String geopoint, String infos) {
        return new PoitiersParkingDto(3, "THEATRE", 320, 50, 84.375,
                geopoint, infos,
                Instant.parse("2026-11-24T16:35:36Z"), Instant.parse("2026-11-24T16:29:24Z"),
                "doc-1", 99L, 1, null);
    }

    @Test
    void toEntity_mapsFields_andSplitsGeopoint() {
        City city = new City();
        city.setName("Poitiers");

        Parking parking = mapper.toEntity(dto("46.58, 0.33", null), city);

        assertThat(parking.getId()).isNull();
        assertThat(parking.getSourceId()).isEqualTo(3);
        assertThat(parking.getExternalId()).isEqualTo("doc-1");
        assertThat(parking.getName()).isEqualTo("THEATRE");
        assertThat(parking.getCapacity()).isEqualTo(320);
        assertThat(parking.getAvailableSpaces()).isEqualTo(50);
        assertThat(parking.getOccupancyRate()).isEqualTo(84.375);
        assertThat(parking.getLatitude()).isEqualTo(46.58);
        assertThat(parking.getLongitude()).isEqualTo(0.33);
        assertThat(parking.getCity()).isSameAs(city);
    }

    @Test
    void toEntity_fallsBackToInfosGeopoint_whenGeopointNull() {
        Parking parking = mapper.toEntity(dto(null, "1.0, 2.0"), new City());

        assertThat(parking.getLatitude()).isEqualTo(1.0);
        assertThat(parking.getLongitude()).isEqualTo(2.0);
    }

    @Test
    void toEntity_leavesCoordsNull_whenNoGeopoint() {
        Parking parking = mapper.toEntity(dto(null, null), new City());

        assertThat(parking.getLatitude()).isNull();
        assertThat(parking.getLongitude()).isNull();
    }

    @Test
    void toEntity_returnsNull_whenAllSourcesNull() {
        assertThat(mapper.toEntity(null, null)).isNull();
    }
}
