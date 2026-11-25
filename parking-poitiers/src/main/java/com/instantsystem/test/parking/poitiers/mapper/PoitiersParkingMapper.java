package com.instantsystem.test.parking.poitiers.mapper;

import com.instantsystem.test.config.MapStructConfig;
import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Maps a {@link PoitiersParkingDto} (Poitiers API) to the {@link Parking} entity.
 * The {@link City} is provided as a parameter, and the geolocation string
 * ({@code "latitude, longitude"}) is split into separate {@code latitude}/{@code longitude} fields.
 */
@Mapper(config = MapStructConfig.class)
public interface PoitiersParkingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sourceId", source = "dto.id")
    @Mapping(target = "externalId", source = "dto.documentId")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "capacity", source = "dto.capacity")
    @Mapping(target = "availableSpaces", source = "dto.availableSpaces")
    @Mapping(target = "occupancyRate", source = "dto.occupancyRate")
    @Mapping(target = "lastUpdatedBase", source = "dto.lastUpdatedBase")
    @Mapping(target = "sourceUpdatedAt", source = "dto.updatedAt")
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "city", source = "city")
    Parking toEntity(PoitiersParkingDto dto, City city);

    @AfterMapping
    default void mapGeopoint(PoitiersParkingDto dto, @MappingTarget Parking parking) {
        String geo = dto.geopoint() != null ? dto.geopoint() : dto.altGeoPoint();
        if (geo != null && geo.contains(",")) {
            String[] parts = geo.split(",", 2);
            parking.setLatitude(Double.parseDouble(parts[0].trim()));
            parking.setLongitude(Double.parseDouble(parts[1].trim()));
        }
    }
}
