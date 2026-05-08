package com.dinebook.backend.dto;

import com.dinebook.backend.model.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDiningRequestStatus(
        @NotNull RequestStatus status
) {}
