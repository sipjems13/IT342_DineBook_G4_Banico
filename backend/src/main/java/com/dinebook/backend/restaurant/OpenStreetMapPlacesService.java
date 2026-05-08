package com.dinebook.backend.restaurant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
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

/**
 * Free restaurant discovery using OpenStreetMap via Overpass API.
 */
@Service
public class OpenStreetMapPlacesService {
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private final RestaurantRepository restaurantRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String overpassTimeout;

    public OpenStreetMapPlacesService(
            RestTemplateBuilder restTemplateBuilder,
            RestaurantRepository restaurantRepository,
            ObjectMapper objectMapper,
            @Value("${overpass.timeout:25}") String overpassTimeout
    ) {
        this.restaurantRepository = restaurantRepository;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.overpassTimeout = overpassTimeout;
    }

    public List<Restaurant> discoverCebuRestaurantsAndUpsert(String cuisineOrKeyword, int maxResults) {
        String query = """
                [out:json][timeout:%s];
                area["name"="Cebu City"]["boundary"="administrative"]->.searchArea;
                (
                  node["amenity"="restaurant"](area.searchArea);
                  way["amenity"="restaurant"](area.searchArea);
                  relation["amenity"="restaurant"](area.searchArea);
                );
                out center tags;
                """.formatted(overpassTimeout);

        String url = UriComponentsBuilder.fromHttpUrl(OVERPASS_URL)
                .queryParam("data", query)
                .build()
                .toUriString();

        String responseBody;
        try {
            responseBody = restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call Overpass API", e);
        }

        if (!StringUtils.hasText(responseBody)) {
            return List.of();
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to parse Overpass response", e);
        }

        JsonNode elements = root.path("elements");
        if (!elements.isArray()) {
            return List.of();
        }

        List<Restaurant> saved = new ArrayList<>();
        int count = 0;

        for (JsonNode el : elements) {
            if (count >= maxResults) break;

            String type = el.path("type").asText(null);
            long id = el.path("id").asLong(-1);
            if (!StringUtils.hasText(type) || id < 0) continue;

            JsonNode tags = el.path("tags");
            String name = tags.path("name").asText(null);
            if (!StringUtils.hasText(name)) continue;

            String externalId = "osm-" + type + "-" + id;

            String cuisine = tags.path("cuisine").asText(null);
            if (!StringUtils.hasText(cuisine)) {
                cuisine = tags.path("food").asText(null);
            }
            if (!StringUtils.hasText(cuisine)) {
                cuisine = StringUtils.hasText(cuisineOrKeyword) ? cuisineOrKeyword : "Restaurant";
            }

            String addrStreet = tags.path("addr:street").asText(null);
            String addrHousenumber = tags.path("addr:housenumber").asText(null);
            String addrCity = tags.path("addr:city").asText(null);

            String location = "Cebu City";
            if (StringUtils.hasText(addrCity) && addrCity.toLowerCase(Locale.ROOT).contains("cebu")) {
                location = addrCity;
            }
            if (StringUtils.hasText(addrStreet)) {
                location = addrStreet;
                if (StringUtils.hasText(addrHousenumber)) {
                    location = location + " " + addrHousenumber;
                }
                location = location + ", Cebu City";
            }

            String imageUrl = tags.path("image").asText(null);
            if (!StringUtils.hasText(imageUrl)) {
                imageUrl = tags.path("website").asText(null);
            }

            Restaurant restaurant = restaurantRepository.findByGooglePlaceId(externalId)
                    .orElseGet(() -> {
                        Restaurant created = new Restaurant();
                        created.setGooglePlaceId(externalId);
                        return created;
                    });

            restaurant.setName(name);
            restaurant.setCuisine(cuisine);
            restaurant.setLocation(location);
            if (restaurant.getImageUrl() == null && StringUtils.hasText(imageUrl)) {
                restaurant.setImageUrl(imageUrl);
            }

            saved.add(restaurantRepository.save(restaurant));
            count++;
        }

        return saved;
    }
}
