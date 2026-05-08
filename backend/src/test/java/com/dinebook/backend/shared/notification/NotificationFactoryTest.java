package com.dinebook.backend.shared.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NotificationFactory Unit Tests")
class NotificationFactoryTest {

    private final EmailNotification emailNotification = new EmailNotification();
    private final NotificationFactory factory = new NotificationFactory(emailNotification);

    @Test
    @DisplayName("TC-NOTIF-01: createNotification EMAIL returns EmailNotification")
    void createNotification_email_returnsEmailNotification() {
        Notification notification = factory.createNotification("EMAIL");
        assertThat(notification).isInstanceOf(EmailNotification.class);
    }

    @Test
    @DisplayName("TC-NOTIF-02: createNotification case-insensitive EMAIL works")
    void createNotification_emailLowercase_returnsEmailNotification() {
        Notification notification = factory.createNotification("email");
        assertThat(notification).isInstanceOf(EmailNotification.class);
    }

    @Test
    @DisplayName("TC-NOTIF-03: createNotification unknown type throws IllegalArgumentException")
    void createNotification_unknownType_throwsException() {
        assertThatThrownBy(() -> factory.createNotification("SMS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown notification type");
    }
}
