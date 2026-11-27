package com.instantsystem.parking.poitiers.service;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import com.instantsystem.parking.database.repository.CityRepository;
import com.instantsystem.parking.database.repository.ParkingReadRepository;
import com.instantsystem.parking.database.repository.ParkingWriteRepository;
import com.instantsystem.parking.dto.CityName;
import com.instantsystem.parking.poitiers.PoitiersClient;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import com.instantsystem.parking.poitiers.mapper.PoitiersParkingMapper;
import com.instantsystem.parking.sync.ParkingSyncService;
import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Synchronises the Poitiers parkings with the local database.
 * <p>
 * The source is the reference: parkings are upserted on their business key (Poitiers {@code Id}) and
 * parkings no longer present in the source are deleted. Invalid records are skipped individually so that
 * one bad record never prevents the others from being refreshed. An empty payload is treated as a source
 * failure and leaves the last known data untouched.
 * <p>
 * The remote call is made outside of the database transaction, so that no connection is held while waiting
 * for the source.
 */
@Service
public class PoitiersParkingSyncService implements ParkingSyncService {
    private static final Logger log = LoggerFactory.getLogger(PoitiersParkingSyncService.class);
    private static final CityName CITY_NAME = CityName.POITIERS;

    private final PoitiersParkingMapper poitiersParkingMapper = Mappers.getMapper(PoitiersParkingMapper.class);

    private final PoitiersClient poitiersClient;
    private final ParkingWriteRepository parkingWriteRepository;
    private final ParkingReadRepository parkingReadRepository;
    private final CityRepository cityRepository;
    private final TransactionTemplate transactionTemplate;

    public PoitiersParkingSyncService(PoitiersClient poitiersClient,
                                      CityRepository cityRepository,
                                      ParkingWriteRepository parkingWriteRepository,
                                      ParkingReadRepository parkingReadRepository,
                                      PlatformTransactionManager transactionManager) {
        this.poitiersClient = poitiersClient;
        this.cityRepository = cityRepository;
        this.parkingWriteRepository = parkingWriteRepository;
        this.parkingReadRepository = parkingReadRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void syncParkings() {
        PoitiersParkingListResponse response = poitiersClient.getParkings();
        if (response == null || response.results() == null || response.results().isEmpty()) {
            log.warn("Empty response from Poitiers API, skipping sync and keeping last known data");
            return;
        }
        transactionTemplate.executeWithoutResult(status -> saveParkings(response.results()));
    }

    private void saveParkings(List<PoitiersParkingDto> results) {
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
