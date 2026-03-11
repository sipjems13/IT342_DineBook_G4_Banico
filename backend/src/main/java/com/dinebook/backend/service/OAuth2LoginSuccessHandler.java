package com.dinebook.backend.service;

import com.dinebook.backend.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String supabaseAnonKey;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final RestTemplate restTemplate;

    public OAuth2LoginSuccessHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof OAuth2User oAuth2User)) {
            response.sendRedirect(frontendUrl + "/auth/callback?error=oauth2_principal_invalid");
            return;
        }

        try {
            String token = exchangeOAuth2UserForToken(oAuth2User);
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            response.sendRedirect(frontendUrl + "/auth/callback?token=" + encodedToken);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    e.getStatusCode(),
                    "OAuth2 login failed: " + e.getResponseBodyAsString()
            );
        }
    }

    private String exchangeOAuth2UserForToken(OAuth2User oAuth2User) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=id_token&provider=google";

        HttpHeaders headers = createHeaders();
        Map<String, String> body = Map.of(
                "id_token", oAuth2User.getAttribute("id_token") != null
                        ? oAuth2User.getAttribute("id_token")
                        : "",
                "access_token", oAuth2User.getAttribute("access_token") != null
                        ? oAuth2User.getAttribute("access_token")
                        : ""
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);

        Map<?, ?> responseBody = responseEntity.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token exchange failed");
        }

        return (String) responseBody.get("access_token");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);
        return headers;
    }
}