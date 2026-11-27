package com.instantsystem.parking.poitiers;

import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "poitiers", url = "${parking.poitiers.url}")
public interface PoitiersClient {

    @GetMapping("/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines")
    PoitiersParkingListResponse getParkings();

}
