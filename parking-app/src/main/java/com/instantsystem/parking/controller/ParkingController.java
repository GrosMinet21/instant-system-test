package com.instantsystem.parking.controller;

import com.instantsystem.parking.dto.ParkingsResponse;
import com.instantsystem.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/parkings")
@Tag(name = "Parkings", description = "Real-time parking availability")
public class ParkingController {

    /**
     * Maximum search radius, in kilometres. Arbitrary value matching a "parkings near me" use case.
     */
    static final String MAX_RADIUS_KM = "10";

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping
    @Operation(summary = "List nearby parkings",
            description = "Returns parkings within radiusKm of the given position, sorted by distance.")
    public ParkingsResponse getParkings(
            @Parameter(description = "User latitude")
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @Parameter(description = "User longitude")
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lon,
            @Parameter(description = "Search radius in kilometres (max " + MAX_RADIUS_KM + ")")
            @RequestParam @Positive @DecimalMax(MAX_RADIUS_KM) double radiusKm) {
        return new ParkingsResponse(parkingService.getParkings(lat, lon, radiusKm));
    }
}
