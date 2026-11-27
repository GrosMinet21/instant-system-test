package com.instantsystem.parking.controller;

import com.instantsystem.parking.dto.ParkingDto;
import com.instantsystem.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/parkings")
@Tag(name = "Parkings", description = "Real-time parking availability")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping
    @Operation(summary = "List nearby parkings",
            description = "Returns parkings within radiusKm of the given position, sorted by distance.")
    public List<ParkingDto> getParkings(
            @Parameter(description = "User latitude") @RequestParam double lat,
            @Parameter(description = "User longitude") @RequestParam double lon,
            @Parameter(description = "Search radius in kilometres") @RequestParam double radiusKm) {
        return parkingService.getParkings(lat, lon, radiusKm);
    }
}
