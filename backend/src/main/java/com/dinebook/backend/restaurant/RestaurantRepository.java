package com.dinebook.backend.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(String location, String cuisine);
    List<Restaurant> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrCuisineContainingIgnoreCase(
            String name, String location, String cuisine);
    Optional<Restaurant> findByGooglePlaceId(String googlePlaceId);
}
