package com.instantsystem.parking.poitiers;

import com.instantsystem.parking.dto.CityName;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingDto;
import com.instantsystem.parking.poitiers.dto.PoitiersParkingListResponse;
import com.instantsystem.parking.sync.ParkingSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoitiersParkingSourceTest {

    private static final int PAGE_SIZE = 2;
    private static final String NEXT = "https://data.grandpoitiers.fr/data-fair/api/v1/datasets/abc/lines"
            + "?size=2&after=1795776226128%2C2754122073096996";

    @Mock
    private PoitiersClient poitiersClient;

    private PoitiersParkingSource source;

    @BeforeEach
    void setUp() {
        source = new PoitiersParkingSource(poitiersClient, PAGE_SIZE);
    }

    private static PoitiersParkingDto dto(Integer id, String name, String geopoint, String altGeoPoint) {
        return new PoitiersParkingDto(id, name, 320, 50, 84.375, geopoint, altGeoPoint,
                Instant.parse("2026-11-27T10:43:46.128Z"));
    }

    private static PoitiersParkingDto dto(Integer id, String name) {
        return dto(id, name, "46.5838, 0.3378", null);
    }

    @Test
    void city_isPoitiers() {
        assertThat(source.city()).isEqualTo(CityName.POITIERS);
    }

    @Test
    void fetchParkings_mapsPoitiersLinesToSnapshots() {
        when(poitiersClient.getParkings(PAGE_SIZE)).thenReturn(new PoitiersParkingListResponse(3, null, List.of(
                dto(3, "THEATRE", "46.58383455409422, 0.33779491061805567", null),
                dto(1, "NOTRE DAME", null, "46.5835, 0.3450"),
                dto(5, "GARE EFFIA", "not, a-number", null))));

        List<ParkingSnapshot> snapshots = source.fetchParkings();

        assertThat(snapshots).containsExactly(
                new ParkingSnapshot("3", "THEATRE", 320, 50, 84.375, 46.58383455409422, 0.33779491061805567,
                        Instant.parse("2026-11-27T10:43:46.128Z")),
                new ParkingSnapshot("1", "NOTRE DAME", 320, 50, 84.375, 46.5835, 0.3450,
                        Instant.parse("2026-11-27T10:43:46.128Z")),
                new ParkingSnapshot("5", "GARE EFFIA", 320, 50, 84.375, null, null,
                        Instant.parse("2026-11-27T10:43:46.128Z")));
    }

    @Test
    void fetchParkings_followsNextLinksUntilTheLastPage() {
        when(poitiersClient.getParkings(PAGE_SIZE))
                .thenReturn(new PoitiersParkingListResponse(3, NEXT, List.of(dto(3, "THEATRE"), dto(1, "NOTRE DAME"))));
        // The next link is followed as is.
        when(poitiersClient.getParkingsNextPage(URI.create(NEXT)))
                .thenReturn(new PoitiersParkingListResponse(3, null, List.of(dto(5, "GARE EFFIA"))));

        List<ParkingSnapshot> snapshots = source.fetchParkings();

        assertThat(snapshots).extracting(ParkingSnapshot::name).containsExactly("THEATRE", "NOTRE DAME", "GARE EFFIA");
    }

    @Test
    void fetchParkings_incompletePayload_fails() {
        // total announces 3 lines, but there is no next page.
        when(poitiersClient.getParkings(PAGE_SIZE))
                .thenReturn(new PoitiersParkingListResponse(3, null, List.of(dto(3, "THEATRE"), dto(1, "NOTRE DAME"))));

        assertThatThrownBy(source::fetchParkings)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2 of 3");
    }

    @Test
    void fetchParkings_emptyNextPage_fails() {
        when(poitiersClient.getParkings(PAGE_SIZE))
                .thenReturn(new PoitiersParkingListResponse(3, NEXT, List.of(dto(3, "THEATRE"), dto(1, "NOTRE DAME"))));
        when(poitiersClient.getParkingsNextPage(URI.create(NEXT))).thenReturn(null);

        assertThatThrownBy(source::fetchParkings).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fetchParkings_nullResponse_returnsEmptyList() {
        when(poitiersClient.getParkings(PAGE_SIZE)).thenReturn(null);

        assertThat(source.fetchParkings()).isEmpty();
        verify(poitiersClient).getParkings(PAGE_SIZE);
    }

    @Test
    void fetchParkings_nullResults_returnsEmptyList() {
        when(poitiersClient.getParkings(PAGE_SIZE)).thenReturn(new PoitiersParkingListResponse(0, null, null));

        assertThat(source.fetchParkings()).isEmpty();
    }
}
