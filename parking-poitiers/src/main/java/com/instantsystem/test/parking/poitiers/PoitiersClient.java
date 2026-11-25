package com.instantsystem.test.parking.poitiers;

import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "poitiers", url = "https://data.grandpoitiers.fr")
public interface PoitiersClient {

    @GetMapping("/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines")
    PoitiersParkingListResponse getParkings();

}
