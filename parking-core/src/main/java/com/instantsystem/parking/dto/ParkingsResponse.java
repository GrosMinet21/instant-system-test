package com.instantsystem.parking.dto;

import java.util.List;

/**
 * Response envelope of the proximity search. Wrapping the list (instead of returning a bare JSON array) lets
 * the API gain metadata later (pagination, search area...) without breaking existing clients.
 */
public record ParkingsResponse(List<ParkingDto> parkings) {
}
