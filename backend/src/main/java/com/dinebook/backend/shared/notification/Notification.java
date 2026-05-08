package com.dinebook.backend.shared.notification;

public interface Notification {
    void send(String recipient, String message);
}
