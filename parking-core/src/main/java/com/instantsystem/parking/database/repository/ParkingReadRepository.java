package com.instantsystem.parking.database.repository;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ParkingReadRepository extends Repository<Parking, Long> {
    List<Parking> findByCity(City city);
    List<Parking> findAll();
}
