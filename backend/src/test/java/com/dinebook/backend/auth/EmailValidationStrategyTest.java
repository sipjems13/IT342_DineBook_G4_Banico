package com.dinebook.backend.auth;

import com.dinebook.backend.auth.dto.RegisterRequest;
import com.dinebook.backend.auth.strategy.EmailValidationStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("EmailValidationStrategy Unit Tests")
class EmailValidationStrategyTest {

    private final EmailValidationStrategy strategy = new EmailValidationStrategy();

    @Test
    @DisplayName("TC-AUTH-01: Valid email passes validation")
    void validate_validEmail_noException() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", "Test User");
        assertThatNoException().isThrownBy(() -> strategy.validate(request));
    }

    @Test
    @DisplayName("TC-AUTH-02: Email without @ throws IllegalArgumentException")
    void validate_emailWithoutAt_throwsException() {
        RegisterRequest request = new RegisterRequest("invalidemail", "password123", "Test User");
        assertThatThrownBy(() -> strategy.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("TC-AUTH-03: Null email throws IllegalArgumentException")
    void validate_nullEmail_throwsException() {
        RegisterRequest request = new RegisterRequest(null, "password123", "Test User");
        assertThatThrownBy(() -> strategy.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("TC-AUTH-04: Empty email throws IllegalArgumentException")
    void validate_emptyEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("", "password123", "Test User");
        assertThatThrownBy(() -> strategy.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }
}
