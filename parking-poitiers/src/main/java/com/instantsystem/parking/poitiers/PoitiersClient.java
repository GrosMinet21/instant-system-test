package com.instantsystem.parking.poitiers;

import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.net.URI;

/**
 * Poitiers real-time parkings endpoint (Spring HTTP interface, implemented by a {@code RestClient} — see
 * {@link PoitiersClientConfig}). The full endpoint URL is configured with {@code parking.poitiers.url}, so the source
 * can move (dataset renamed, API version bump, proxy...) without a code change, as long as the format stays.
 * <p>
 * The data-fair {@code /lines} endpoint is paginated: each page gives the full URL of the following one in its
 * {@code next} field (absent on the last page).
 */
public interface PoitiersClient {

    /**
     * First page, from the configured URL.
     */
    @GetExchange
    PoitiersParkingListResponse getParkings(@RequestParam("size") int size);

    /**
     * Following page: the {@code next} URL of the previous page, called as is (a {@link URI} argument overrides the
     * configured URL).
     */
    @GetExchange
    PoitiersParkingListResponse getParkingsNextPage(URI next);

}
