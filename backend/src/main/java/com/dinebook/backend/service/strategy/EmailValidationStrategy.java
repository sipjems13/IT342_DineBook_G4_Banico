package com.dinebook.backend.service.strategy;

import com.dinebook.backend.dto.RegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class EmailValidationStrategy implements ValidationStrategy {
    @Override
    public void validate(RegisterRequest request) {
        if (request.email() == null || !request.email().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
