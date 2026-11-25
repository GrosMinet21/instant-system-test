package com.instantsystem.test.parking.service;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.database.repository.ParkingReadRepository;
import com.instantsystem.test.parking.dto.CityName;
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
    private ParkingReadRepository parkingRepository;

    @InjectMocks
    private ParkingService parkingService;

    private Parking parking(String name, double lat, double lon) {
        City city = new City();
        city.setName(CityName.POITIERS);
        Parking p = new Parking();
        p.setName(name);
        p.setLatitude(lat);
        p.setLongitude(lon);
        p.setCity(city);
        return p;
    }

    @Test
    void getParkings_returnsEmptyWhenNoneInRadius() {
        when(parkingRepository.findAll()).thenReturn(List.of(parking("FAR", 48.85, 2.35)));

        assertThat(parkingService.getParkings(46.58, 0.34, 1.0)).isEmpty();
    }

    @Test
    void getParkings_filtersAndSortsByDistance() {
        Parking near = parking("NEAR", 46.5802, 0.3404);
        Parking far = parking("FAR", 46.6000, 0.4000);
        when(parkingRepository.findAll()).thenReturn(List.of(far, near));

        List<ParkingDto> result = parkingService.getParkings(46.5800, 0.3400, 2.0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("NEAR");
        assertThat(result.get(0).distanceMeters()).isPositive();
    }

    @Test
    void getParkings_excludesParkingsWithoutCoordinates() {
        City city = new City();
        city.setName(CityName.POITIERS);
        Parking noCoords = new Parking();
        noCoords.setName("NO_COORDS");
        noCoords.setCity(city);
        when(parkingRepository.findAll()).thenReturn(List.of(noCoords));

        assertThat(parkingService.getParkings(46.58, 0.34, 10.0)).isEmpty();
    }
}
