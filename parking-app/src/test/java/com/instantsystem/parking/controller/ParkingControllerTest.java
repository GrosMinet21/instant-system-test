package com.instantsystem.parking.controller;

import com.instantsystem.parking.dto.ParkingDto;
import com.instantsystem.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ParkingControllerTest {

    private static final String URL = "/v1/parkings";

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private ParkingController parkingController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(parkingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getParkings_returnsParkingsFromService() throws Exception {
        ParkingDto dto = new ParkingDto("THEATRE", 320, 50, 84.3, 46.58, 0.34, "POITIERS", 250.0,
                Instant.parse("2026-11-27T09:52:40Z"));
        when(parkingService.getParkings(46.58, 0.34, 1.0)).thenReturn(List.of(dto));

        mockMvc.perform(get(URL).param("lat", "46.58").param("lon", "0.34").param("radiusKm", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkings[0].name").value("THEATRE"))
                .andExpect(jsonPath("$.parkings[0].city").value("POITIERS"))
                .andExpect(jsonPath("$.parkings[0].distanceMeters").value(250.0))
                .andExpect(jsonPath("$.parkings[0].lastUpdated").value("2026-11-27T09:52:40Z"));
    }

    @Test
    void getParkings_maxRadius_isAccepted() throws Exception {
        when(parkingService.getParkings(46.58, 0.34, 10.0)).thenReturn(List.of());

        mockMvc.perform(get(URL).param("lat", "46.58").param("lon", "0.34").param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkings").isEmpty());
    }

    @Test
    void getParkings_missingParam_returns400() throws Exception {
        mockMvc.perform(get(URL).param("lat", "46.58").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("lon")));
    }

    @Test
    void getParkings_notANumber_returns400() throws Exception {
        mockMvc.perform(get(URL).param("lat", "notANumber").param("lon", "0.34").param("radiusKm", "1.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("lat")));
    }

    @ParameterizedTest(name = "lat={0}, lon={1}, radiusKm={2} -> {3}")
    @CsvSource({
            "90.1,  0.34,   1,    lat",
            "-90.1, 0.34,   1,    lat",
            "46.58, 180.1,  1,    lon",
            "46.58, -180.1, 1,    lon",
            "46.58, 0.34,   0,    radiusKm",
            "46.58, 0.34,   -1,   radiusKm",
            "46.58, 0.34,   10.1, radiusKm"
    })
    void getParkings_outOfRangeParam_returns400(String lat, String lon, String radiusKm, String invalidParam)
            throws Exception {
        mockMvc.perform(get(URL).param("lat", lat).param("lon", lon).param("radiusKm", radiusKm)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString(invalidParam)));

        verify(parkingService, never()).getParkings(anyDouble(), anyDouble(), anyDouble());
    }
}
