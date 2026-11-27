package com.instantsystem.parking.poitiers.service;

import com.instantsystem.parking.poitiers.PoitiersClient;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import com.instantsystem.parking.sync.ParkingSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Synchronises the Poitiers parkings with the local database.
 * <p>
 * Fetches the source outside of any transaction (no DB connection held while waiting for the remote API), then
 * delegates persistence to {@link PoitiersParkingWriter}. An empty payload is treated as a source failure and
 * leaves the last known data untouched.
 */
@Service
public class PoitiersParkingSyncService implements ParkingSyncService {
    private static final Logger log = LoggerFactory.getLogger(PoitiersParkingSyncService.class);

    private final PoitiersClient poitiersClient;
    private final PoitiersParkingWriter poitiersParkingWriter;

    public PoitiersParkingSyncService(PoitiersClient poitiersClient, PoitiersParkingWriter poitiersParkingWriter) {
        this.poitiersClient = poitiersClient;
        this.poitiersParkingWriter = poitiersParkingWriter;
    }

    @Override
    public void syncParkings() {
        PoitiersParkingListResponse response = poitiersClient.getParkings();
        if (response == null || response.results() == null || response.results().isEmpty()) {
            log.warn("Empty response from Poitiers API, skipping sync and keeping last known data");
            return;
        }
        poitiersParkingWriter.saveParkings(response.results());
    }
}
