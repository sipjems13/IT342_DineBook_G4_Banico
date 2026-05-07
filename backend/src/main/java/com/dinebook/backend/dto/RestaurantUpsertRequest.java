package com.dinebook.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RestaurantUpsertRequest(
        @NotBlank String name,
        @NotBlank String location,
        @NotBlank String cuisine,
        String imageUrl
) {}
