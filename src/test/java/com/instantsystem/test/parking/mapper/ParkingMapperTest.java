package com.instantsystem.test.parking.mapper;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.dto.ParkingDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingMapperTest {

    private final ParkingMapper mapper = Mappers.getMapper(ParkingMapper.class);

    private Parking parking() {
        City city = new City();
        city.setName("Poitiers");
        Parking p = new Parking();
        p.setId(1L);
        p.setName("THEATRE");
        p.setCapacity(320);
        p.setAvailableSpaces(50);
        p.setOccupancyRate(84.375);
        p.setLatitude(46.58);
        p.setLongitude(0.33);
        p.setCity(city);
        return p;
    }

    @Test
    void toDto_flattensCityName() {
        ParkingDto dto = mapper.toDto(parking());

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("THEATRE");
        assertThat(dto.capacity()).isEqualTo(320);
        assertThat(dto.availableSpaces()).isEqualTo(50);
        assertThat(dto.occupancyRate()).isEqualTo(84.375);
        assertThat(dto.latitude()).isEqualTo(46.58);
        assertThat(dto.longitude()).isEqualTo(0.33);
        assertThat(dto.cityName()).isEqualTo("Poitiers");
    }

    @Test
    void toDto_returnsNull_whenEntityNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDtos_mapsList() {
        List<ParkingDto> dtos = mapper.toDtos(List.of(parking()));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).cityName()).isEqualTo("Poitiers");
    }

    @Test
    void toDtos_returnsNull_whenListNull() {
        assertThat(mapper.toDtos(null)).isNull();
    }

    @Test
    void toDto_leavesCityNameNull_whenCityNull() {
        Parking p = new Parking();
        p.setId(1L);
        p.setName("THEATRE");
        p.setCity(null);

        ParkingDto dto = mapper.toDto(p);

        assertThat(dto.name()).isEqualTo("THEATRE");
        assertThat(dto.cityName()).isNull();
    }
}
