package com.dinebook.backend.service;

import com.dinebook.backend.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class AuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String supabaseAnonKey;

    private final RestTemplate restTemplate;

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> register(RegisterRequest request) {
        String url = supabaseUrl + "/auth/v1/signup";

        HttpHeaders headers = createHeaders();
        HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.postForEntity(url, entity, Map.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Registration failed: " + e.getResponseBodyAsString()
            );
        }
    }

    public ResponseEntity<?> login(LoginRequest request) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        HttpHeaders headers = createHeaders();
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.postForEntity(url, entity, Map.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "Login failed: " + e.getResponseBodyAsString()
            );
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);
        return headers;
    }
}