package com.dinebook.backend.shared.notification;

import org.springframework.stereotype.Component;

@Component
public class EmailNotification implements Notification {
    @Override
    public void send(String recipient, String message) {
        // Simulate sending email
        System.out.println("Sending Email to " + recipient + ": " + message);
    }
}
