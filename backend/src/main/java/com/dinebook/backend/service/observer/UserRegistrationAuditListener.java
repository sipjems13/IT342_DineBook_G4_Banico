package com.dinebook.backend.service.observer;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

@Component
public class UserRegistrationAuditListener implements ApplicationListener<UserRegisteredEvent> {
    @Override
    public void onApplicationEvent(@NonNull UserRegisteredEvent event) {
        // Observer reacting to the event
        System.out.println("AUDIT LOG: User registered successfully with email - " + event.getEmail());
    }
}
