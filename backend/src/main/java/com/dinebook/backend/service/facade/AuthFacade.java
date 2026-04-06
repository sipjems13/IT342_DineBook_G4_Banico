package com.dinebook.backend.service.facade;

import com.dinebook.backend.dto.LoginRequest;
import com.dinebook.backend.dto.RegisterRequest;
import com.dinebook.backend.service.adapter.AuthClient;
import com.dinebook.backend.service.notification.Notification;
import com.dinebook.backend.service.notification.NotificationFactory;
import com.dinebook.backend.service.observer.UserRegisteredEvent;
import com.dinebook.backend.service.strategy.ValidationStrategy;
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

    public AuthFacade(AuthClient authClient,
                      List<ValidationStrategy> validationStrategies,
                      NotificationFactory notificationFactory,
                      ApplicationEventPublisher eventPublisher) {
        this.authClient = authClient;
        this.validationStrategies = validationStrategies;
        this.notificationFactory = notificationFactory;
        this.eventPublisher = eventPublisher;
    }

    public ResponseEntity<?> register(RegisterRequest request) {
        // Strategy Pattern: validate the request using all configured strategies
        for (ValidationStrategy strategy : validationStrategies) {
            strategy.validate(request);
        }

        // Adapter Pattern: call the underlying auth provider (e.g., Supabase)
        ResponseEntity<?> response = authClient.registerUser(request);

        if (response.getStatusCode().is2xxSuccessful()) {
            // Observer Pattern: Publish event so other components can react
            eventPublisher.publishEvent(new UserRegisteredEvent(this, request.email()));

            // Factory Pattern: Create specific notification implementation and send it
            Notification notification = notificationFactory.createNotification("EMAIL");
            notification.send(request.email(), "Welcome to DineBook! Your registration was successful.");
        }

        return response;
    }

    public ResponseEntity<?> login(LoginRequest request) {
        // Adapter Pattern: abstracting specific provider for login
        return authClient.authenticateUser(request);
    }
}
