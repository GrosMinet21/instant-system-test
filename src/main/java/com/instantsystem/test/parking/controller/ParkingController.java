package com.instantsystem.test.parking.controller;

import com.instantsystem.test.parking.dto.ParkingDto;
import com.instantsystem.test.parking.service.ParkingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cities/{city}/parkings")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    /**
     * Liste tous les parkings rattachés à la ville donnée.
     */
    @GetMapping
    public List<ParkingDto> getParkingsByCity(@PathVariable String city) {
        return parkingService.getParkings(city);
    }
}
