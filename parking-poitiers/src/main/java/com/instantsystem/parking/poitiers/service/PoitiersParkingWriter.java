package com.instantsystem.parking.poitiers.service;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import com.instantsystem.parking.database.repository.CityRepository;
import com.instantsystem.parking.database.repository.ParkingReadRepository;
import com.instantsystem.parking.database.repository.ParkingWriteRepository;
import com.instantsystem.parking.dto.CityName;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.poitiers.mapper.PoitiersParkingMapper;
import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PoitiersParkingWriter {
    private static final Logger log = LoggerFactory.getLogger(PoitiersParkingWriter.class);
    private static final CityName CITY_NAME = CityName.POITIERS;

    private final PoitiersParkingMapper poitiersParkingMapper = Mappers.getMapper(PoitiersParkingMapper.class);

    private final CityRepository cityRepository;
    private final ParkingReadRepository parkingReadRepository;
    private final ParkingWriteRepository parkingWriteRepository;

    public PoitiersParkingWriter(CityRepository cityRepository,
                                 ParkingReadRepository parkingReadRepository,
                                 ParkingWriteRepository parkingWriteRepository) {
        this.cityRepository = cityRepository;
        this.parkingReadRepository = parkingReadRepository;
        this.parkingWriteRepository = parkingWriteRepository;
    }

    @Transactional
    public void saveParkings(List<PoitiersParkingDto> results) {
        City city = cityRepository.findByName(CITY_NAME).orElseGet(this::createCity);

        // Parkings still in this map after processing the payload are gone from the source.
        Map<String, Parking> stale = parkingReadRepository.findByCity(city).stream()
                .collect(Collectors.toMap(Parking::getExternalId, Function.identity()));
        Map<String, Parking> upserted = new LinkedHashMap<>();
        List<String> withoutCoordinates = new ArrayList<>();

        for (PoitiersParkingDto dto : results) {
            if (isSyncable(dto, upserted.keySet())) {
                String externalId = String.valueOf(dto.id());
                Parking parking = stale.remove(externalId);
                if (parking == null) {
                    parking = new Parking();
                }
                poitiersParkingMapper.updateEntity(dto, city, parking);
                if (parking.getLatitude() == null || parking.getLongitude() == null) {
                    withoutCoordinates.add(parking.getName());
                }
                upserted.put(externalId, parking);
            }
        }

        parkingWriteRepository.saveAll(upserted.values());
        parkingWriteRepository.deleteAll(stale.values());

        if (!withoutCoordinates.isEmpty()) {
            log.warn("Poitiers parkings without valid coordinates (excluded from proximity search): {}",
                    withoutCoordinates.size());
        }
        log.info("Parking sync for {}: {} upserted, {} removed", CITY_NAME, upserted.size(), stale.size());
    }

    /**
     * A record is syncable if it has a business key and a name, and its key was not already seen in this payload.
     */
    private boolean isSyncable(PoitiersParkingDto dto, Set<String> alreadySynced) {
        if (dto.id() == null || dto.name() == null) {
            log.warn("Skipping Poitiers parking with missing Id or Nom: {}", dto);
            return false;
        }
        if (alreadySynced.contains(String.valueOf(dto.id()))) {
            log.warn("Skipping duplicate Poitiers parking Id {} ({})", dto.id(), dto.name());
            return false;
        }
        return true;
    }

    private @NonNull City createCity() {
        City c = new City();
        c.setName(CITY_NAME);
        return cityRepository.save(c);
    }
}
