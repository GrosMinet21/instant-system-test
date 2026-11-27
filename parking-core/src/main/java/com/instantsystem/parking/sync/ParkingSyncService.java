package com.instantsystem.parking.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingSyncService {
    private static final Logger log = LoggerFactory.getLogger(ParkingSyncService.class);

    private final ParkingSnapshotWriter parkingSnapshotWriter;

    public ParkingSyncService(ParkingSnapshotWriter parkingSnapshotWriter) {
        this.parkingSnapshotWriter = parkingSnapshotWriter;
    }

    public void sync(ParkingSource source) {
        List<ParkingSnapshot> snapshots = source.fetchParkings();
        if (snapshots == null || snapshots.isEmpty()) {
            log.warn("Empty payload from the {} source, keeping last known data", source.city());
            return;
        }
        parkingSnapshotWriter.write(source.city(), snapshots);
    }
}
