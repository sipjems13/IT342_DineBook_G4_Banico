package com.dinebook.backend.service.strategy;

import com.dinebook.backend.dto.RegisterRequest;

public interface ValidationStrategy {
    void validate(RegisterRequest request);
}
