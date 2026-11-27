package com.instantsystem.parking.poitiers.dto;

import java.util.List;

public record PoitiersParkingListResponse(Integer total, List<PoitiersParkingDto> results) {
}
