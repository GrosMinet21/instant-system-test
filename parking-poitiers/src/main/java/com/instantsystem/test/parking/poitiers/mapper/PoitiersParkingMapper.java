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
 * Mappe un {@link PoitiersParkingDto} (API Poitiers) vers l'entité {@link Parking}.
 * La {@link City} de rattachement est fournie en paramètre, et la géolocalisation
 * ({@code "latitude, longitude"}) est décomposée en {@code latitude}/{@code longitude}.
 */
@Mapper(config = MapStructConfig.class)
public interface PoitiersParkingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sourceId", source = "dto.id")
    @Mapping(target = "externalId", source = "dto.documentId")
    @Mapping(target = "name", source = "dto.nom")
    @Mapping(target = "capacity", source = "dto.capacite")
    @Mapping(target = "availableSpaces", source = "dto.places")
    @Mapping(target = "occupancyRate", source = "dto.tauxDoccupation")
    @Mapping(target = "lastUpdatedBase", source = "dto.derniereMiseAJourBase")
    @Mapping(target = "sourceUpdatedAt", source = "dto.updatedAt")
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "city", source = "city")
    Parking toEntity(PoitiersParkingDto dto, City city);

    @AfterMapping
    default void mapGeopoint(PoitiersParkingDto dto, @MappingTarget Parking parking) {
        String geo = dto.geopoint() != null ? dto.geopoint() : dto.infosParkingsGeoPoint();
        if (geo != null && geo.contains(",")) {
            String[] parts = geo.split(",", 2);
            parking.setLatitude(Double.parseDouble(parts[0].trim()));
            parking.setLongitude(Double.parseDouble(parts[1].trim()));
        }
    }
}
