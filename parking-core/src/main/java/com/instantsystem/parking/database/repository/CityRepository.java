package com.instantsystem.parking.database.repository;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.dto.CityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByName(CityName name);
}
