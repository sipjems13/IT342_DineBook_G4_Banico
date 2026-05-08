package com.dinebook.backend.auth.strategy;

import com.dinebook.backend.auth.dto.RegisterRequest;

public interface ValidationStrategy {
    void validate(RegisterRequest request);
}
