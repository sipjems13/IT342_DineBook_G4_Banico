package com.dinebook.backend.auth.observer;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationAuditListener implements ApplicationListener<UserRegisteredEvent> {
    @Override
    public void onApplicationEvent(@NonNull UserRegisteredEvent event) {
        System.out.println("AUDIT LOG: User registered successfully with email - " + event.getEmail());
    }
}
