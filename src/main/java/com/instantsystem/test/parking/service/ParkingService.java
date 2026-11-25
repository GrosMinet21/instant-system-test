package com.instantsystem.test.parking.service;

import com.instantsystem.test.parking.database.repository.ParkingReadRepository;
import com.instantsystem.test.parking.dto.ParkingDto;
import com.instantsystem.test.parking.mapper.ParkingMapper;
import com.instantsystem.test.parking.util.HaversineUtil;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;

@Service
public class ParkingService {

    private final ParkingMapper parkingMapper = Mappers.getMapper(ParkingMapper.class);
    private final ParkingReadRepository parkingRepository;

    public ParkingService(ParkingReadRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
    }

    public List<ParkingDto> getParkings(double lat, double lon, double radiusKm) {
        double radiusMeters = radiusKm * 1000;

        return parkingRepository.findAll().stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .map(p -> {
                    double distance = HaversineUtil.distanceMeters(lat, lon, p.getLatitude(), p.getLongitude());
                    return new AbstractMap.SimpleEntry<>(p, distance);
                })
                .filter(e -> e.getValue() <= radiusMeters)
                .sorted(java.util.Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
                .map(e -> parkingMapper.toDto(e.getKey(), e.getValue()))
                .toList();
    }
}
