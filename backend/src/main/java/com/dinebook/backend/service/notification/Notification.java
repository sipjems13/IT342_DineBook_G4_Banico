package com.dinebook.backend.service.notification;

public interface Notification {
    void send(String recipient, String message);
}
