package com.dinebook.backend.dto;

import com.dinebook.backend.model.RequestStatus;

import java.time.LocalDateTime;

public record DiningRequestDto(
        Long id,
        Long restaurantId,
        String restaurantName,
        String dinerEmail,
        LocalDateTime requestedDateTime,
        int guests,
        RequestStatus status,
        LocalDateTime createdAt
) {}
