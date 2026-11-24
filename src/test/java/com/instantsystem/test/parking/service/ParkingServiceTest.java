package com.instantsystem.test.parking.service;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.database.repository.ParkingRepository;
import com.instantsystem.test.parking.dto.ParkingDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingRepository parkingRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    void getParkings_mapsEntitiesToDtos() {
        City city = new City();
        city.setName("Poitiers");
        Parking parking = new Parking();
        parking.setId(1L);
        parking.setName("THEATRE");
        parking.setCapacity(320);
        parking.setCity(city);
        when(parkingRepository.findByCityNameIgnoreCase("Poitiers")).thenReturn(List.of(parking));

        List<ParkingDto> result = parkingService.getParkings("Poitiers");

        assertThat(result).hasSize(1);
        ParkingDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("THEATRE");
        assertThat(dto.capacity()).isEqualTo(320);
        assertThat(dto.cityName()).isEqualTo("Poitiers");
    }

    @Test
    void getParkings_returnsEmptyWhenNone() {
        when(parkingRepository.findByCityNameIgnoreCase("Lyon")).thenReturn(List.of());

        assertThat(parkingService.getParkings("Lyon")).isEmpty();
    }
}
