package com.instantsystem.test.parking.mapper;

import com.instantsystem.test.config.MapStructConfig;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.dto.ParkingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mappe l'entité {@link Parking} vers sa représentation d'API {@link ParkingDto}.
 */
@Mapper(config = MapStructConfig.class)
public interface ParkingMapper {

    @Mapping(target = "cityName", source = "city.name")
    ParkingDto toDto(Parking parking);

    List<ParkingDto> toDtos(List<Parking> parkings);
}
