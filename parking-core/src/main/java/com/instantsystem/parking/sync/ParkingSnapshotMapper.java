package com.instantsystem.parking.sync;

import com.instantsystem.parking.config.MapStructConfig;
import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ParkingSnapshotMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "snapshot.name")
    void updateEntity(ParkingSnapshot snapshot, City city, @MappingTarget Parking parking);
}
