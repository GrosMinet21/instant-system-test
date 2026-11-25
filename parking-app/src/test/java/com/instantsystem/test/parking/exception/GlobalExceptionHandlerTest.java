package com.instantsystem.test.parking.exception;

import com.instantsystem.test.parking.controller.GlobalExceptionHandler;
import com.instantsystem.test.parking.controller.ParkingController;
import com.instantsystem.test.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

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
    void invalidLatParam_returns400() throws Exception {
        mockMvc.perform(get("/parkings")
                        .param("lat", "notANumber")
                        .param("lon", "0.34")
                        .param("radiusKm", "1.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void illegalArgument_returns400() throws Exception {
        when(parkingService.getParkings(anyDouble(), anyDouble(), anyDouble()))
                .thenThrow(new IllegalArgumentException("radiusKm must be positive"));

        mockMvc.perform(get("/parkings")
                        .param("lat", "46.5").param("lon", "0.3").param("radiusKm", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("radiusKm must be positive"));
    }
}
