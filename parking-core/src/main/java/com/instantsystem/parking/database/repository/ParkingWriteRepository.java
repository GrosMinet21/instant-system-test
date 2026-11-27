package com.instantsystem.parking.database.repository;

import com.instantsystem.parking.database.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingWriteRepository extends JpaRepository<Parking, Long> {

    Optional<Parking> findByExternalId(String externalId);
}
