package com.dinebook.backend.auth;

import com.dinebook.backend.auth.adapter.AuthClient;
import com.dinebook.backend.auth.dto.LoginRequest;
import com.dinebook.backend.auth.dto.RegisterRequest;
import com.dinebook.backend.auth.observer.UserRegisteredEvent;
import com.dinebook.backend.auth.strategy.ValidationStrategy;
import com.dinebook.backend.shared.notification.Notification;
import com.dinebook.backend.shared.notification.NotificationFactory;
import com.dinebook.backend.user.UserRoleService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthFacade {

    private final AuthClient authClient;
    private final List<ValidationStrategy> validationStrategies;
    private final NotificationFactory notificationFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRoleService userRoleService;

    public AuthFacade(AuthClient authClient,
                      List<ValidationStrategy> validationStrategies,
                      NotificationFactory notificationFactory,
                      ApplicationEventPublisher eventPublisher,
                      UserRoleService userRoleService) {
        this.authClient = authClient;
        this.validationStrategies = validationStrategies;
        this.notificationFactory = notificationFactory;
        this.eventPublisher = eventPublisher;
        this.userRoleService = userRoleService;
    }

    public ResponseEntity<?> register(RegisterRequest request) {
        // Strategy Pattern: validate the request using all configured strategies
        for (ValidationStrategy strategy : validationStrategies) {
            strategy.validate(request);
        }

        // Adapter Pattern: call the underlying auth provider (e.g., Supabase)
        ResponseEntity<?> response = authClient.registerUser(request);

        if (response.getStatusCode().is2xxSuccessful()) {
            userRoleService.ensureUser(request.email());
            // Observer Pattern: publish event so other components can react
            eventPublisher.publishEvent(new UserRegisteredEvent(this, request.email()));

            // Factory Pattern: create specific notification implementation and send it
            Notification notification = notificationFactory.createNotification("EMAIL");
            notification.send(request.email(), "Welcome to DineBook! Your registration was successful.");
        }

        return response;
    }

    public ResponseEntity<?> login(LoginRequest request) {
        // Adapter Pattern: abstracting specific provider for login
        ResponseEntity<?> response = authClient.authenticateUser(request);

        if (response.getStatusCode().is2xxSuccessful()) {
            userRoleService.ensureUser(request.email());
        }

        return response;
    }
}
