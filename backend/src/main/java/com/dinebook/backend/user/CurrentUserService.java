package com.dinebook.backend.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class CurrentUserService {
    private final UserRoleService userRoleService;

    public CurrentUserService(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    public String requireEmail(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Missing authentication token");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getSubject();
        }

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Email claim is missing");
        }

        userRoleService.ensureUser(email);
        return email;
    }

    public UserRole requireRole(Authentication authentication) {
        String email = requireEmail(authentication);
        return userRoleService.resolveRole(email);
    }

    public void requireStaff(Authentication authentication) {
        UserRole role = requireRole(authentication);
        if (role != UserRole.STAFF) {
            throw new ResponseStatusException(FORBIDDEN, "Staff role required");
        }
    }

    public void requireDiner(Authentication authentication) {
        UserRole role = requireRole(authentication);
        if (role != UserRole.DINER) {
            throw new ResponseStatusException(FORBIDDEN, "Diner role required");
        }
    }

    public void requireAdmin(Authentication authentication) {
        UserRole role = requireRole(authentication);
        if (role != UserRole.ADMIN) {
            throw new ResponseStatusException(FORBIDDEN, "Admin role required");
        }
    }
}

