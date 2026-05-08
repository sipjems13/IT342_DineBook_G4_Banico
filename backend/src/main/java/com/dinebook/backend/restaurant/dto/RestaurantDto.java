package com.dinebook.backend.restaurant.dto;

public record RestaurantDto(
        Long id,
        String name,
        String location,
        String cuisine,
        String imageUrl,
        Double rating
) {}
