package com.dinebook.backend.controller;

import com.dinebook.backend.model.UserRole;
import com.dinebook.backend.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final CurrentUserService currentUserService;

    public UserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        String email = currentUserService.requireEmail(authentication);
        UserRole role = currentUserService.requireRole(authentication);
        return Map.of(
                "email", email,
                "role", role.name()
        );
    }
}
