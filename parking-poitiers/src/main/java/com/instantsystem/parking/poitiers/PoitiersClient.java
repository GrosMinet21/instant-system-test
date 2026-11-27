package com.instantsystem.parking.poitiers;

import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Poitiers real-time parkings endpoint. The full endpoint URL is configured with {@code parking.poitiers.url}, so the
 * source can move (dataset renamed, API version bump, proxy...) without a code change, as long as the format stays.
 */
@FeignClient(name = "poitiers", url = "${parking.poitiers.url}")
public interface PoitiersClient {

    @GetMapping
    PoitiersParkingListResponse getParkings();

}
