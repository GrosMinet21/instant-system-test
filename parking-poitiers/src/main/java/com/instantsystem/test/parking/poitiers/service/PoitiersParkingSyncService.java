package com.instantsystem.test.parking.poitiers.service;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.database.repository.CityRepository;
import com.instantsystem.test.parking.database.repository.ParkingWriteRepository;
import com.instantsystem.test.parking.dto.CityName;
import com.instantsystem.test.parking.poitiers.PoitiersClient;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingListResponse;
import com.instantsystem.test.parking.poitiers.mapper.PoitiersParkingMapper;
import com.instantsystem.test.parking.sync.ParkingSyncService;
import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PoitiersParkingSyncService implements ParkingSyncService {
    private static final Logger log = LoggerFactory.getLogger(PoitiersParkingSyncService.class);
    private static final CityName CITY_NAME = CityName.POITIERS;

    private final PoitiersParkingMapper poitiersParkingMapper = Mappers.getMapper(PoitiersParkingMapper.class);

    private final PoitiersClient poitiersClient;
    private final ParkingWriteRepository parkingRepository;
    private final CityRepository cityRepository;


    public PoitiersParkingSyncService(PoitiersClient poitiersClient,
                                      CityRepository cityRepository,
                                      ParkingWriteRepository parkingRepository) {
        this.poitiersClient = poitiersClient;
        this.cityRepository = cityRepository;
        this.parkingRepository = parkingRepository;
    }

    @Transactional
    public void syncParkings() {
        PoitiersParkingListResponse response = poitiersClient.getParkings();
        if (response == null || response.results() == null) {
            log.warn("Empty response from Poitiers API, skipping sync");
            return;
        }

        City city = cityRepository.findByName(CITY_NAME).orElseGet(this::createCity);

        int count = 0;
        for (PoitiersParkingDto dto : response.results()) {
            Parking parking = poitiersParkingMapper.toEntity(dto, city);
            // Upsert: reuse the existing id if this parking is already in the database.
            parkingRepository.findByExternalId(dto.documentId()).ifPresent(existing -> parking.setId(existing.getId()));
            parkingRepository.save(parking);
            count++;
        }
        log.info("Parking sync for {}: {} records upserted", CITY_NAME, count);
    }

    private @NonNull City createCity() {
        City c = new City();
        c.setName(CITY_NAME);
        return cityRepository.save(c);
    }
}
