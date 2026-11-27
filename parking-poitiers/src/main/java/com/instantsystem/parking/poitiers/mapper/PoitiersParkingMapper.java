package com.instantsystem.parking.poitiers.mapper;

import com.instantsystem.parking.config.MapStructConfig;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.sync.ParkingSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface PoitiersParkingMapper {

    @Mapping(target = "externalId", source = "id")
    @Mapping(target = "lastUpdated", source = "updatedAt")
    @Mapping(target = "latitude", source = "dto", qualifiedByName = "latitude")
    @Mapping(target = "longitude", source = "dto", qualifiedByName = "longitude")
    ParkingSnapshot toSnapshot(PoitiersParkingDto dto);

    @Named("latitude")
    default Double latitude(PoitiersParkingDto dto) {
        double[] coordinates = coordinates(dto);
        return coordinates == null ? null : coordinates[0];
    }

    @Named("longitude")
    default Double longitude(PoitiersParkingDto dto) {
        double[] coordinates = coordinates(dto);
        return coordinates == null ? null : coordinates[1];
    }

    /**
     * @return {@code [latitude, longitude]}, or {@code null} if the geopoint is missing or malformed
     */
    private static double[] coordinates(PoitiersParkingDto dto) {
        String geo = dto.geopoint() != null ? dto.geopoint() : dto.altGeoPoint();
        if (geo == null) {
            return null;
        }
        String[] parts = geo.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new double[]{Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim())};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
