package com.instantsystem.test.parking.poitiers.service;

import com.instantsystem.test.parking.database.entity.City;
import com.instantsystem.test.parking.database.entity.Parking;
import com.instantsystem.test.parking.database.repository.CityRepository;
import com.instantsystem.test.parking.database.repository.ParkingRepository;
import com.instantsystem.test.parking.poitiers.PoitiersClient;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.test.parking.poitiers.dto.PoitiersParkingListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoitiersParkingsServiceTest {

    @Mock
    private PoitiersClient poitiersClient;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private ParkingRepository parkingRepository;

    @InjectMocks
    private PoitiersParkingsService service;

    private PoitiersParkingDto dto(String docId, String nom) {
        return new PoitiersParkingDto(3, nom, 320, 50, 84.3,
                "46.5, 0.3", null, null, null, docId, 1L, 1, null);
    }

    @Test
    void syncParkings_nullResponse_doesNothing() {
        when(poitiersClient.getParkings()).thenReturn(null);

        service.syncParkings();

        verify(parkingRepository, never()).save(any());
    }

    @Test
    void syncParkings_nullResults_doesNothing() {
        when(poitiersClient.getParkings()).thenReturn(new PoitiersParkingListResponse(0, null));

        service.syncParkings();

        verify(parkingRepository, never()).save(any());
    }

    @Test
    void syncParkings_createsCityWhenAbsent_andInsertsNewParking() {
        when(poitiersClient.getParkings())
                .thenReturn(new PoitiersParkingListResponse(1, List.of(dto("doc-1", "THEATRE"))));
        when(cityRepository.findByName("Poitiers")).thenReturn(Optional.empty());
        City saved = new City();
        saved.setName("Poitiers");
        when(cityRepository.save(any(City.class))).thenReturn(saved);
        when(parkingRepository.findByExternalId("doc-1")).thenReturn(Optional.empty());

        service.syncParkings();

        verify(cityRepository).save(any(City.class));
        ArgumentCaptor<Parking> captor = ArgumentCaptor.forClass(Parking.class);
        verify(parkingRepository).save(captor.capture());
        Parking persisted = captor.getValue();
        assertThat(persisted.getId()).isNull(); // nouveau -> pas d'id réutilisé
        assertThat(persisted.getName()).isEqualTo("THEATRE");
        assertThat(persisted.getCity()).isEqualTo(saved);
    }

    @Test
    void syncParkings_reusesExistingId_whenParkingAlreadyPresent() {
        when(poitiersClient.getParkings())
                .thenReturn(new PoitiersParkingListResponse(1, List.of(dto("doc-1", "THEATRE"))));
        City city = new City();
        city.setName("Poitiers");
        when(cityRepository.findByName("Poitiers")).thenReturn(Optional.of(city));
        Parking existing = new Parking();
        existing.setId(42L);
        when(parkingRepository.findByExternalId("doc-1")).thenReturn(Optional.of(existing));

        service.syncParkings();

        ArgumentCaptor<Parking> captor = ArgumentCaptor.forClass(Parking.class);
        verify(parkingRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(42L); // upsert -> id réutilisé
    }
}
