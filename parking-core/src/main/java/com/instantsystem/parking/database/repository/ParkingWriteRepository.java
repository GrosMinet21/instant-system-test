package com.instantsystem.parking.database.repository;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingWriteRepository extends JpaRepository<Parking, Long> {

}
