package com.dinebook.backend.service.adapter;

import org.springframework.http.ResponseEntity;
import com.dinebook.backend.dto.LoginRequest;
import com.dinebook.backend.dto.RegisterRequest;

public interface AuthClient {
    ResponseEntity<?> registerUser(RegisterRequest request);
    ResponseEntity<?> authenticateUser(LoginRequest request);
}
