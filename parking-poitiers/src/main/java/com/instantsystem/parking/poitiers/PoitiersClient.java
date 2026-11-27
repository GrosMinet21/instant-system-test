package com.instantsystem.parking.poitiers;

import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

/**
 * Poitiers real-time parkings endpoint. The full endpoint URL is configured with {@code parking.poitiers.url}, so the
 * source can move (dataset renamed, API version bump, proxy...) without a code change, as long as the format stays.
 * <p>
 * The data-fair {@code /lines} endpoint is paginated: each page gives the full URL of the following one in its
 * {@code next} field (absent on the last page).
 */
@FeignClient(name = "poitiers", url = "${parking.poitiers.url}")
public interface PoitiersClient {

    /**
     * First page, from the configured URL.
     */
    @GetMapping
    PoitiersParkingListResponse getParkings(@RequestParam("size") int size);

    /**
     * Following page: the {@code next} URL of the previous page, called as is (a {@link URI} parameter overrides the
     * configured URL).
     */
    @GetMapping
    PoitiersParkingListResponse getParkingsNextPage(URI next);

}
