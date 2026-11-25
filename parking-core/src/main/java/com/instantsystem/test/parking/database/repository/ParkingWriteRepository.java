package com.instantsystem.test.parking.database.repository;

import com.instantsystem.test.parking.database.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingWriteRepository extends JpaRepository<Parking, Long> {

    Optional<Parking> findByExternalId(String externalId);
}
