package com.dinebook.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateDiningRequest(
        @NotNull Long restaurantId,
        @NotNull @Future LocalDateTime requestedDateTime,
        @Min(1) int guests
) {}
