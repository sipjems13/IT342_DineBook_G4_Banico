package com.dinebook.backend.booking;

import com.dinebook.backend.booking.dto.CreateDiningRequest;
import com.dinebook.backend.booking.dto.DiningRequestDto;
import com.dinebook.backend.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dining-requests")
public class DiningRequestController {
    private final DiningRequestService diningRequestService;
    private final CurrentUserService currentUserService;

    public DiningRequestController(DiningRequestService diningRequestService, CurrentUserService currentUserService) {
        this.diningRequestService = diningRequestService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public DiningRequestDto createRequest(@Valid @RequestBody CreateDiningRequest request, Authentication authentication) {
        String email = currentUserService.requireEmail(authentication);
        return diningRequestService.create(email, request);
    }

    @GetMapping("/my")
    public List<DiningRequestDto> myRequests(Authentication authentication) {
        String email = currentUserService.requireEmail(authentication);
        return diningRequestService.myRequests(email);
    }
}
