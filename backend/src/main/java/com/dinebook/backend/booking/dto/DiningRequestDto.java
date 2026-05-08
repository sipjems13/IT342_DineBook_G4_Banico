package com.dinebook.backend.booking.dto;

import com.dinebook.backend.booking.RequestStatus;

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
