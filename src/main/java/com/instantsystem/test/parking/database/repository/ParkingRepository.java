package com.instantsystem.test.parking.database.repository;

import com.instantsystem.test.parking.database.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingRepository extends JpaRepository<Parking, Long> {

    Optional<Parking> findByExternalId(String externalId);

    List<Parking> findByCityNameIgnoreCase(String cityName);
}
