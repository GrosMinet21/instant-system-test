package com.instantsystem.test.parking.service;

import com.instantsystem.test.parking.database.repository.ParkingRepository;
import com.instantsystem.test.parking.dto.ParkingDto;
import com.instantsystem.test.parking.mapper.ParkingMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingService {
    private final ParkingMapper parkingMapper = Mappers.getMapper(ParkingMapper.class);
    private final ParkingRepository parkingRepository;

    public ParkingService(ParkingRepository parkingRepository) {
        this.parkingRepository = parkingRepository;
    }

    public List<ParkingDto> getParkings(String city) {
        return parkingMapper.toDtos(parkingRepository.findByCityNameIgnoreCase(city));
    }
}
