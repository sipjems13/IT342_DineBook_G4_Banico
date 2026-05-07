package com.dinebook.backend.service;

import com.dinebook.backend.model.Restaurant;
import com.dinebook.backend.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class GooglePlacesService {
    private static final String PLACES_NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    private final RestaurantRepository restaurantRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String apiKey;
    private final double cebuLat;
    private final double cebuLng;
    private final int radiusMeters;

    public GooglePlacesService(
            RestTemplateBuilder restTemplateBuilder,
            RestaurantRepository restaurantRepository,
            ObjectMapper objectMapper,
            @Value("${google.places.apiKey:}") String apiKey,
            @Value("${google.places.cebu.lat:10.3157}") double cebuLat,
            @Value("${google.places.cebu.lng:123.8854}") double cebuLng,
            @Value("${google.places.cebu.radiusMeters:15000}") int radiusMeters
    ) {
        this.restaurantRepository = restaurantRepository;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.cebuLat = cebuLat;
        this.cebuLng = cebuLng;
        this.radiusMeters = radiusMeters;
    }

    public List<Restaurant> discoverCebuRestaurantsAndUpsert(String keyword, int maxResults) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Google Places API key is not configured. Add GOOGLE_PLACES_API_KEY env var / application.properties."
            );
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(PLACES_NEARBY_SEARCH_URL)
                .queryParam("location", cebuLat + "," + cebuLng)
                .queryParam("radius", radiusMeters)
                .queryParam("type", "restaurant")
                .queryParam("key", apiKey);

        if (StringUtils.hasText(keyword)) {
            uriBuilder.queryParam("keyword", keyword);
        }

        String url = uriBuilder.toUriString();

        String responseBody;
        try {
            responseBody = restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to call Google Places",
                    e
            );
        }

        if (!StringUtils.hasText(responseBody)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from Google Places");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to parse Google Places response", e);
        }

        String status = root.path("status").asText();
        if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
            String errorMessage = root.path("error_message").asText(null);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google Places returned status=" + status + (errorMessage == null ? "" : (": " + errorMessage))
            );
        }

        JsonNode resultsNode = root.path("results");
        if (!resultsNode.isArray()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected Google Places response shape");
        }

        List<PlaceCandidate> candidates = new ArrayList<>();
        int limit = Math.min(maxResults, resultsNode.size());
        for (int i = 0; i < limit; i++) {
            JsonNode r = resultsNode.get(i);
            String placeId = r.path("place_id").asText(null);
            if (!StringUtils.hasText(placeId)) continue;

            String name = r.path("name").asText("Unknown");
            String vicinity = r.path("vicinity").asText(null);

            if (!StringUtils.hasText(vicinity)) {
                vicinity = r.path("formatted_address").asText(null);
            }

            candidates.add(new PlaceCandidate(placeId, name, vicinity));
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        // "Cebu City only" enforcement: Nearby Search can include nearby cities; we keep only results
        // that look like Cebu.
        List<PlaceCandidate> cebuCandidates = candidates.stream()
                .filter(c -> c.vicinity() != null && c.vicinity().toLowerCase(Locale.ROOT).contains("cebu"))
                .toList();

        List<PlaceCandidate> chosen = cebuCandidates.isEmpty() ? candidates : cebuCandidates;

        String cuisineValue = StringUtils.hasText(keyword) ? keyword : "Restaurant";

        List<Restaurant> saved = new ArrayList<>();
        for (PlaceCandidate candidate : chosen) {
            Restaurant restaurant = restaurantRepository.findByGooglePlaceId(candidate.placeId())
                    .orElseGet(() -> {
                        Restaurant created = new Restaurant();
                        created.setGooglePlaceId(candidate.placeId());
                        return created;
                    });

            restaurant.setName(candidate.name());
            restaurant.setLocation(ensureCebuCity(candidate.vicinity()));

            // Only override cuisine if it's currently blank (keeps staff-added cuisine if they later edit it).
            if (!StringUtils.hasText(restaurant.getCuisine())) {
                restaurant.setCuisine(cuisineValue);
            }

            // Keep existing imageUrl if it exists.

            saved.add(restaurantRepository.save(restaurant));
        }

        return saved;
    }

    private String ensureCebuCity(String location) {
        String base = location;
        if (!StringUtils.hasText(base)) {
            base = "Cebu City";
        }
        base = base.trim();

        String lc = base.toLowerCase(Locale.ROOT);
        if (lc.contains("cebu city")) return base;
        if (lc.contains("cebu")) return base + ", Cebu City";
        return base + ", Cebu City";
    }

    private record PlaceCandidate(String placeId, String name, String vicinity) {}
}

