package com.dinebook.backend.booking.dto;

import com.dinebook.backend.booking.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDiningRequestStatus(
        @NotNull RequestStatus status
) {}
