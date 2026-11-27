package com.instantsystem.parking.mapper;

import com.instantsystem.parking.config.MapStructConfig;
import com.instantsystem.parking.database.entity.Parking;
import com.instantsystem.parking.dto.ParkingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ParkingMapper {

    @Mapping(target = "city", source = "parking.city.name")
    @Mapping(target = "distanceMeters", source = "distanceMeters")
    ParkingDto toDto(Parking parking, Double distanceMeters);
}
