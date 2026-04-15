package com.dinebook.backend.service.notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationFactory {
    private final EmailNotification emailNotification;

    public NotificationFactory(EmailNotification emailNotification) {
        this.emailNotification = emailNotification;
    }

    public Notification createNotification(String type) {
        if ("EMAIL".equalsIgnoreCase(type)) {
            return emailNotification;
        }
        throw new IllegalArgumentException("Unknown notification type: " + type);
    }
}
