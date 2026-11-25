package com.instantsystem.test.parking.database.repository;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.dto.CityName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByName(CityName name);
}
