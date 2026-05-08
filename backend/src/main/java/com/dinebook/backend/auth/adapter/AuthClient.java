package com.dinebook.backend.auth.adapter;

import com.dinebook.backend.auth.dto.LoginRequest;
import com.dinebook.backend.auth.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthClient {
    ResponseEntity<?> registerUser(RegisterRequest request);
    ResponseEntity<?> authenticateUser(LoginRequest request);
}
