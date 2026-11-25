package com.instantsystem.test.parking.controller;

import com.instantsystem.test.parking.dto.CityName;
import com.instantsystem.test.parking.dto.ParkingDto;
import com.instantsystem.test.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ParkingControllerTest {

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private ParkingController parkingController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(parkingController).build();
    }

    @Test
    void getParkings_returnsListFromService() throws Exception {
        ParkingDto dto = new ParkingDto("THEATRE", 320, 50, 84.3, 46.58, 0.34, CityName.POITIERS, 0.0);
        when(parkingService.getParkings(46.58, 0.34, 1.0)).thenReturn(List.of(dto));

        mockMvc.perform(get("/parkings").param("lat", "46.58").param("lon", "0.34").param("radiusKm", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("THEATRE"))
                .andExpect(jsonPath("$[0].capacity").value(320));
    }

    @Test
    void getParkings_withDistanceInResponse() throws Exception {
        ParkingDto dto = new ParkingDto("THEATRE", 320, 50, 84.3, 46.58, 0.34, CityName.POITIERS, 250.0);
        when(parkingService.getParkings(46.58, 0.34, 1.0)).thenReturn(List.of(dto));

        mockMvc.perform(get("/parkings").param("lat", "46.58").param("lon", "0.34").param("radiusKm", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanceMeters").value(250.0));
    }

    @Test
    void getParkings_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/parkings").param("lat", "46.58"))
                .andExpect(status().isBadRequest());
    }
}
