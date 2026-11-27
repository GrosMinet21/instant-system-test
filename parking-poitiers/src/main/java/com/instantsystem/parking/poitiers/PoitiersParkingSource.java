package com.instantsystem.parking.poitiers;

import com.instantsystem.parking.dto.CityName;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import com.instantsystem.parking.poitiers.mapper.PoitiersParkingMapper;
import com.instantsystem.parking.sync.ParkingSnapshot;
import com.instantsystem.parking.sync.ParkingSource;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Poitiers parking source (Grand Poitiers data-fair real-time dataset).
 * <p>
 * Fetches every page of the dataset by following the {@code next} links, and fails if fewer lines than the announced
 * total could be fetched — an incomplete list would otherwise delete the missing parkings.
 */
@Component
public class PoitiersParkingSource implements ParkingSource {

    /**
     * Safety net against a source that would keep returning a {@code next} link.
     */
    private static final int MAX_PAGES = 100;

    private final PoitiersParkingMapper poitiersParkingMapper = Mappers.getMapper(PoitiersParkingMapper.class);

    private final PoitiersClient poitiersClient;
    private final int pageSize;

    public PoitiersParkingSource(PoitiersClient poitiersClient,
                                 @Value("${parking.poitiers.page-size:1000}") int pageSize) {
        this.poitiersClient = poitiersClient;
        this.pageSize = pageSize;
    }

    @Override
    public CityName city() {
        return CityName.POITIERS;
    }

    @Override
    public List<ParkingSnapshot> fetchParkings() {
        return fetchAllPages().stream()
                .map(poitiersParkingMapper::toSnapshot)
                .toList();
    }

    private List<PoitiersParkingDto> fetchAllPages() {
        PoitiersParkingListResponse page = poitiersClient.getParkings(pageSize);
        if (page == null || page.results() == null) {
            return List.of();
        }
        int total = page.total() != null ? page.total() : page.results().size();
        List<PoitiersParkingDto> parkings = new ArrayList<>(page.results());
        int pageCount = 1;
        String next = page.next();

        while (next != null && pageCount < MAX_PAGES) {
            page = poitiersClient.getParkingsNextPage(URI.create(next));
            if (page == null || page.results() == null) {
                throw new IllegalStateException("Poitiers API returned an empty page after " + pageCount + " pages");
            }
            parkings.addAll(page.results());
            pageCount++;
            next = page.next();
        }

        if (parkings.size() < total) {
            throw new IllegalStateException("Incomplete Poitiers payload: " + parkings.size() + " of " + total
                    + " parkings fetched");
        }
        return parkings;
    }
}
