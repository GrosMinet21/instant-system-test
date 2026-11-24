package com.instantsystem.test.parking.controller;

import com.instantsystem.test.parking.dto.ParkingDto;
import com.instantsystem.test.parking.service.ParkingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingControllerTest {

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private ParkingController parkingController;

    @Test
    void getParkingsByCity_delegatesToService() {
        ParkingDto dto = new ParkingDto(1L, "THEATRE", 320, 50, 84.3,
                46.5, 0.3, null, null, "Poitiers");
        when(parkingService.getParkings("Poitiers")).thenReturn(List.of(dto));

        List<ParkingDto> result = parkingController.getParkingsByCity("Poitiers");

        assertThat(result).containsExactly(dto);
    }
}
