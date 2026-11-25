package com.instantsystem.test.parking.database.repository;

import com.instantsystem.test.parking.database.entity.Parking;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ParkingReadRepository extends Repository<Parking, Long> {

    List<Parking> findAll();
}
