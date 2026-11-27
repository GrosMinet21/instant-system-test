package com.instantsystem.parking.sync;

import com.instantsystem.parking.dto.CityName;

import java.util.List;

/**
 * A city parking data source: the only thing to implement to support a new city.
 * <p>
 * Implementations only fetch their source and map it to {@link ParkingSnapshot}s. Everything else — validation,
 * upsert, deletion of the parkings no longer listed, transaction, safeguards against a failing source — is handled
 * generically by {@link ParkingSyncService}, identically for every city.
 */
public interface ParkingSource {

    /**
     * @return the city served by this source
     */
    CityName city();

    /**
     * Fetches the current state of every parking of the city.
     * <p>
     * Must return the <b>complete</b> list: a parking missing from it is considered closed and deleted. If the source
     * cannot be fully read (unavailable, incomplete pagination...), throw an exception instead: the synchronisation
     * of this city is then skipped and the last known data kept.
     *
     * @return the parkings of the city; an empty list is treated as a source failure
     */
    List<ParkingSnapshot> fetchParkings();
}
