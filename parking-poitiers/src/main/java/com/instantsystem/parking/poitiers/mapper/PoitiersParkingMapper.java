package com.instantsystem.parking.poitiers.mapper;

import com.instantsystem.parking.config.MapStructConfig;
import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Maps a {@link PoitiersParkingDto} (Poitiers API) onto a {@link Parking} entity.
 * <p>
 * The entity is updated in place so that the sync can reuse managed entities. The business key is the
 * Poitiers {@code Id} — the data-fair {@code _id} is regenerated on every dataset refresh and must not be used.
 * The geolocation string ({@code "latitude, longitude"}) is split into {@code latitude}/{@code longitude};
 * a missing or malformed value leaves both coordinates {@code null} instead of failing the whole sync.
 */
@Mapper(config = MapStructConfig.class)
public interface PoitiersParkingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", source = "dto.id")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "capacity", source = "dto.capacity")
    @Mapping(target = "availableSpaces", source = "dto.availableSpaces")
    @Mapping(target = "occupancyRate", source = "dto.occupancyRate")
    @Mapping(target = "lastUpdatedBase", source = "dto.lastUpdatedBase")
    @Mapping(target = "sourceUpdatedAt", source = "dto.updatedAt")
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "city", source = "city")
    void updateEntity(PoitiersParkingDto dto, City city, @MappingTarget Parking parking);

    @AfterMapping
    default void mapGeopoint(PoitiersParkingDto dto, @MappingTarget Parking parking) {
        parking.setLatitude(null);
        parking.setLongitude(null);

        String geo = dto.geopoint() != null ? dto.geopoint() : dto.altGeoPoint();
        if (geo == null) {
            return;
        }
        String[] parts = geo.split(",");
        if (parts.length != 2) {
            return;
        }
        try {
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());
            if (Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180) {
                parking.setLatitude(latitude);
                parking.setLongitude(longitude);
            }
        } catch (NumberFormatException e) {
            // Malformed coordinates: left null, reported by the sync service.
        }
    }
}
