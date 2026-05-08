package com.dinebook.backend.repository;

import com.dinebook.backend.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(String location, String cuisine);
    List<Restaurant> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrCuisineContainingIgnoreCase(
            String name,
            String location,
            String cuisine
    );

    java.util.Optional<Restaurant> findByGooglePlaceId(String googlePlaceId);
}
