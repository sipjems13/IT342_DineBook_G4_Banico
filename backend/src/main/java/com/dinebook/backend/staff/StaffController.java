package com.dinebook.backend.staff;

import com.dinebook.backend.booking.DiningRequestService;
import com.dinebook.backend.booking.RequestStatus;
import com.dinebook.backend.booking.dto.DiningRequestDto;
import com.dinebook.backend.booking.dto.UpdateDiningRequestStatus;
import com.dinebook.backend.restaurant.RestaurantService;
import com.dinebook.backend.restaurant.dto.RestaurantDto;
import com.dinebook.backend.restaurant.dto.RestaurantUpsertRequest;
import com.dinebook.backend.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/staff")
public class StaffController {
    private final CurrentUserService currentUserService;
    private final RestaurantService restaurantService;
    private final DiningRequestService diningRequestService;

    public StaffController(
            CurrentUserService currentUserService,
            RestaurantService restaurantService,
            DiningRequestService diningRequestService
    ) {
        this.currentUserService = currentUserService;
        this.restaurantService = restaurantService;
        this.diningRequestService = diningRequestService;
    }

    @PostMapping("/restaurants")
    public RestaurantDto createRestaurant(@Valid @RequestBody RestaurantUpsertRequest request, Authentication auth) {
        currentUserService.requireStaff(auth);
        return restaurantService.create(request);
    }

    @PutMapping("/restaurants/{id}")
    public RestaurantDto updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantUpsertRequest request,
            Authentication auth
    ) {
        currentUserService.requireStaff(auth);
        return restaurantService.update(id, request);
    }

    @DeleteMapping("/restaurants/{id}")
    public void deleteRestaurant(@PathVariable Long id, Authentication auth) {
        currentUserService.requireStaff(auth);
        restaurantService.delete(id);
    }

    @GetMapping("/requests")
    public List<DiningRequestDto> getAllRequests(Authentication auth) {
        currentUserService.requireStaff(auth);
        return diningRequestService.allRequests();
    }

    @PatchMapping("/requests/{id}/status")
    public DiningRequestDto updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiningRequestStatus request,
            Authentication auth
    ) {
        currentUserService.requireStaff(auth);
        return diningRequestService.updateStatus(id, request.status());
    }
}
