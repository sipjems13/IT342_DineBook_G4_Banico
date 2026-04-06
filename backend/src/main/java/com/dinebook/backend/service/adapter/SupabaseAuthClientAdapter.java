package com.dinebook.backend.service.adapter;

import com.dinebook.backend.dto.LoginRequest;
import com.dinebook.backend.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class SupabaseAuthClientAdapter implements AuthClient {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String supabaseAnonKey;

    private final RestTemplate restTemplate;

    public SupabaseAuthClientAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<?> registerUser(RegisterRequest request) {
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

    @Override
    public ResponseEntity<?> authenticateUser(LoginRequest request) {
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
