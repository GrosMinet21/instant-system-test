package com.instantsystem.test.parking.dto;


public record ParkingDto(
        String name,
        Integer capacity,
        Integer availableSpaces,
        Double occupancyRate,
        Double latitude,
        Double longitude,
        CityName city,
        Double distanceMeters
) {
}
