package com.dinebook.backend.service;

import com.dinebook.backend.dto.RestaurantDto;
import com.dinebook.backend.dto.RestaurantUpsertRequest;
import com.dinebook.backend.model.Restaurant;
import com.dinebook.backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final OpenStreetMapPlacesService openStreetMapPlacesService;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            OpenStreetMapPlacesService openStreetMapPlacesService
    ) {
        this.restaurantRepository = restaurantRepository;
        this.openStreetMapPlacesService = openStreetMapPlacesService;
    }

    public List<RestaurantDto> browse(String location, String cuisine, String q) {
        String safeLocation = (location == null || location.trim().isBlank()) ? "Cebu City" : location.trim();
        String safeCuisine = cuisine == null ? "" : cuisine.trim();
        String safeQuery = q == null ? "" : q.trim();

        List<Restaurant> restaurants = safeQuery.isBlank()
                ? restaurantRepository.findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(safeLocation, safeCuisine)
                : restaurantRepository.findByNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrCuisineContainingIgnoreCase(
                        safeQuery, safeQuery, safeQuery
                );

        // Enforce project requirement: show Cebu City restaurants only.
        restaurants = restaurants.stream()
                .filter(r -> r.getLocation() != null && r.getLocation().toLowerCase().contains("cebu"))
                .toList();

        if (restaurants.isEmpty()) {
            // If the database doesn't have matches yet, seed Cebu City restaurants using a free provider.
            String keyword = !safeCuisine.isBlank() ? safeCuisine : (!safeQuery.isBlank() ? safeQuery : null);

            try {
                List<Restaurant> discovered = openStreetMapPlacesService.discoverCebuRestaurantsAndUpsert(keyword, 20);

                return discovered.stream()
                        .map(this::toDto)
                        .toList();
            } catch (Exception e) {
                // Log and return empty rather than poisoning the transaction.
                System.err.println("OpenStreetMap discovery failed: " + e.getMessage());
                return List.of();
            }
        }

        return restaurants.stream().map(this::toDto).toList();
    }

    public RestaurantDto create(RestaurantUpsertRequest request) {
        Restaurant restaurant = new Restaurant();
        copyFields(restaurant, request);
        return toDto(restaurantRepository.save(restaurant));
    }

    public RestaurantDto update(Long id, RestaurantUpsertRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Restaurant not found"));
        copyFields(restaurant, request);
        return toDto(restaurantRepository.save(restaurant));
    }

    public void delete(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Restaurant not found");
        }
        restaurantRepository.deleteById(id);
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Restaurant not found"));
    }

    private void copyFields(Restaurant restaurant, RestaurantUpsertRequest request) {
        restaurant.setName(request.name());
        restaurant.setLocation(request.location());
        restaurant.setCuisine(request.cuisine());
        restaurant.setImageUrl(request.imageUrl());
    }

    private RestaurantDto toDto(Restaurant restaurant) {
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getLocation(),
                restaurant.getCuisine(),
                restaurant.getImageUrl(),
                restaurant.getRating()
        );
    }
}
