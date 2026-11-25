package com.instantsystem.test.parking.poitiers.dto;

import java.util.List;

public record PoitiersParkingListResponse(Integer total, List<PoitiersParkingDto> results) {
}
