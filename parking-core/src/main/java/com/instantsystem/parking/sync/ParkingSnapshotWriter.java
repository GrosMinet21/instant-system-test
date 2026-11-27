package com.instantsystem.parking.sync;

import com.instantsystem.parking.database.entity.City;
import com.instantsystem.parking.database.entity.Parking;
import com.instantsystem.parking.database.repository.CityRepository;
import com.instantsystem.parking.database.repository.ParkingReadRepository;
import com.instantsystem.parking.database.repository.ParkingWriteRepository;
import com.instantsystem.parking.dto.CityName;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Persists the snapshots of a city, in a single transaction. Generic: the same rules apply to every city.
 * <ul>
 *     <li>parkings are upserted on their business key ({@code externalId}, unique per city);</li>
 *     <li>invalid records (missing key or name, duplicate key) are skipped individually;</li>
 *     <li>a parking still listed by the source keeps its last known data if its record is invalid this time;
 *     only parkings no longer listed at all are deleted;</li>
 *     <li>if no record is valid (e.g. the source format changed), nothing is written: the last known data is kept;</li>
 *     <li>out-of-range coordinates are dropped (the parking is kept but excluded from proximity search), and a
 *     {@code lastUpdated} in the future (source clock skew) is capped to the sync time.</li>
 * </ul>
 */
@Service
public class ParkingSnapshotWriter {
    private static final Logger log = LoggerFactory.getLogger(ParkingSnapshotWriter.class);

    private final ParkingSnapshotMapper parkingSnapshotMapper = Mappers.getMapper(ParkingSnapshotMapper.class);

    private final CityRepository cityRepository;
    private final ParkingReadRepository parkingReadRepository;
    private final ParkingWriteRepository parkingWriteRepository;

    public ParkingSnapshotWriter(CityRepository cityRepository,
                                 ParkingReadRepository parkingReadRepository,
                                 ParkingWriteRepository parkingWriteRepository) {
        this.cityRepository = cityRepository;
        this.parkingReadRepository = parkingReadRepository;
        this.parkingWriteRepository = parkingWriteRepository;
    }

    @Transactional
    public void write(CityName cityName, List<ParkingSnapshot> snapshots) {
        City city = cityRepository.findByName(cityName).orElseGet(() -> createCity(cityName));
        Map<String, Parking> existing = parkingReadRepository.findByCity(city).stream()
                .collect(Collectors.toMap(Parking::getExternalId, Function.identity()));
        Instant now = Instant.now();

        Map<String, Parking> upserted = new LinkedHashMap<>();
        int withoutCoordinates = 0;
        for (ParkingSnapshot snapshot : snapshots) {
            if (isValid(cityName, snapshot, upserted.keySet())) {
                Parking parking = existing.getOrDefault(snapshot.externalId(), new Parking());
                parkingSnapshotMapper.updateEntity(snapshot, city, parking);
                if (!hasValidCoordinates(parking)) {
                    parking.setLatitude(null);
                    parking.setLongitude(null);
                    withoutCoordinates++;
                }
                if (parking.getLastUpdated() != null && parking.getLastUpdated().isAfter(now)) {
                    parking.setLastUpdated(now);
                }
                upserted.put(snapshot.externalId(), parking);
            }
        }

        if (upserted.isEmpty()) {
            log.warn("No valid parking in the {} payload ({} records), keeping last known data",
                    cityName, snapshots.size());
            return;
        }

        Set<String> listedIds = snapshots.stream()
                .map(ParkingSnapshot::externalId)
                .filter(externalId -> externalId != null && !externalId.isBlank())
                .collect(Collectors.toSet());
        List<Parking> removed = existing.values().stream()
                .filter(parking -> !listedIds.contains(parking.getExternalId()))
                .toList();

        parkingWriteRepository.saveAll(upserted.values());
        parkingWriteRepository.deleteAll(removed);

        if (withoutCoordinates > 0) {
            log.warn("{} parkings without valid coordinates (excluded from proximity search): {}",
                    cityName, withoutCoordinates);
        }
        log.info("Parking sync for {}: {} upserted, {} removed", cityName, upserted.size(), removed.size());
    }

    /**
     * A record is valid if it has a business key and a name, and its key was not already seen in this payload.
     */
    private boolean isValid(CityName cityName, ParkingSnapshot snapshot, Set<String> alreadyWritten) {
        if (isBlank(snapshot.externalId()) || isBlank(snapshot.name())) {
            log.warn("Skipping {} parking with missing externalId or name: {}", cityName, snapshot);
            return false;
        }
        if (alreadyWritten.contains(snapshot.externalId())) {
            log.warn("Skipping duplicate {} parking {} ({})", cityName, snapshot.externalId(), snapshot.name());
            return false;
        }
        return true;
    }

    private static boolean hasValidCoordinates(Parking parking) {
        Double latitude = parking.getLatitude();
        Double longitude = parking.getLongitude();
        return latitude != null && longitude != null
                && Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private City createCity(CityName cityName) {
        City city = new City();
        city.setName(cityName);
        return cityRepository.save(city);
    }
}
