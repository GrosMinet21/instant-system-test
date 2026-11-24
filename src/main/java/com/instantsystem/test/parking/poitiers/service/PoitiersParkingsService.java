package com.instantsystem.test.parking.poitiers.service;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.database.repository.CityRepository;
import com.instantsystem.test.parking.database.repository.ParkingRepository;
import com.instantsystem.test.parking.poitiers.PoitiersClient;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingListResponse;
import com.instantsystem.test.parking.poitiers.mapper.PoitiersParkingMapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PoitiersParkingsService {
    private static final Logger log = LoggerFactory.getLogger(PoitiersParkingsService.class);
    private static final String CITY_NAME = "Poitiers";

    private final PoitiersParkingMapper poitiersParkingMapper = Mappers.getMapper(PoitiersParkingMapper.class);

    private final PoitiersClient poitiersClient;
    private final ParkingRepository parkingRepository;
    private final CityRepository cityRepository;


    public PoitiersParkingsService(PoitiersClient poitiersClient,
                                   CityRepository cityRepository,
                                   ParkingRepository parkingRepository) {
        this.poitiersClient = poitiersClient;
        this.cityRepository = cityRepository;
        this.parkingRepository = parkingRepository;
    }

    @Transactional
    public void syncParkings() {
        PoitiersParkingListResponse response = poitiersClient.getParkings();
        if (response == null || response.results() == null) {
            log.warn("Réponse vide de l'API Poitiers, synchronisation ignorée");
            return;
        }

        City city = cityRepository.findByName(CITY_NAME).orElseGet(() -> {
            City c = new City();
            c.setName(CITY_NAME);
            return cityRepository.save(c);
        });

        int count = 0;
        for (PoitiersParkingDto dto : response.results()) {
            Parking parking = poitiersParkingMapper.toEntity(dto, city);
            // Upsert : réutilise l'id existant si ce parking est déjà en base.
            parkingRepository.findByExternalId(dto.documentId()).ifPresent(existing -> parking.setId(existing.getId()));
            parkingRepository.save(parking);
            count++;
        }
        log.info("Synchronisation parkings {} : {} enregistrements upsertés", CITY_NAME, count);
    }
}
