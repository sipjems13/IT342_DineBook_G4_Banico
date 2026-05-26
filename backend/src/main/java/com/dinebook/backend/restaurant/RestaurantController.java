package com.dinebook.backend.restaurant;

import com.dinebook.backend.restaurant.dto.RestaurantDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public List<RestaurantDto> browseRestaurants(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false, name = "q") String query
    ) {
        return restaurantService.browse(location, cuisine, query);
    }

    @GetMapping("/{id}")
    public RestaurantDto getRestaurant(@PathVariable Long id) {
        return restaurantService.toDto(restaurantService.findById(id));
    }
}
