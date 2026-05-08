package com.dinebook.backend.restaurant;

import com.dinebook.backend.restaurant.dto.RestaurantDto;
import com.dinebook.backend.restaurant.dto.RestaurantUpsertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantService Unit Tests")
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private OpenStreetMapPlacesService openStreetMapPlacesService;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant cebuRestaurant;

    @BeforeEach
    void setUp() {
        cebuRestaurant = new Restaurant();
        cebuRestaurant.setName("Cebu Lechon House");
        cebuRestaurant.setLocation("Cebu City");
        cebuRestaurant.setCuisine("Filipino");
        cebuRestaurant.setRating(4.5);
    }

    @Test
    @DisplayName("TC-REST-01: browse returns restaurants from DB when found")
    void browse_restaurantsInDb_returnsList() {
        when(restaurantRepository.findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(
                "Cebu City", ""))
                .thenReturn(List.of(cebuRestaurant));

        List<RestaurantDto> result = restaurantService.browse(null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Cebu Lechon House");
    }

    @Test
    @DisplayName("TC-REST-02: browse filters out non-Cebu restaurants")
    void browse_nonCebuRestaurant_filtered() {
        Restaurant manilaRestaurant = new Restaurant();
        manilaRestaurant.setName("Manila Grill");
        manilaRestaurant.setLocation("Manila");
        manilaRestaurant.setCuisine("Filipino");
        manilaRestaurant.setRating(4.0);

        when(restaurantRepository.findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(
                "Cebu City", ""))
                .thenReturn(List.of(manilaRestaurant));
        when(openStreetMapPlacesService.discoverCebuRestaurantsAndUpsert(any(), anyInt()))
                .thenReturn(List.of());

        List<RestaurantDto> result = restaurantService.browse(null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("TC-REST-03: create saves and returns new restaurant")
    void create_validRequest_savesRestaurant() {
        RestaurantUpsertRequest request = new RestaurantUpsertRequest(
                "New Bistro", "Cebu City", "Italian", null);

        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(inv -> {
            Restaurant r = inv.getArgument(0);
            r.setRating(0.0);
            return r;
        });

        RestaurantDto result = restaurantService.create(request);

        assertThat(result.name()).isEqualTo("New Bistro");
        assertThat(result.location()).isEqualTo("Cebu City");
        assertThat(result.cuisine()).isEqualTo("Italian");
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("TC-REST-04: update throws 404 when restaurant not found")
    void update_notFound_throws404() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        RestaurantUpsertRequest request = new RestaurantUpsertRequest(
                "Updated", "Cebu City", "Japanese", null);

        assertThatThrownBy(() -> restaurantService.update(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Restaurant not found");
    }

    @Test
    @DisplayName("TC-REST-05: delete throws 404 when restaurant not found")
    void delete_notFound_throws404() {
        when(restaurantRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> restaurantService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Restaurant not found");
    }

    @Test
    @DisplayName("TC-REST-06: findById throws 404 when restaurant not found")
    void findById_notFound_throws404() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Restaurant not found");
    }
}
