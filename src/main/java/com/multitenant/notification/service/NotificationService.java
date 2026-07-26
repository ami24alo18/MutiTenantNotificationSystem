package com.multitenant.notification.service;

import com.multitenant.notification.dto.NotificationRequest;

public interface NotificationService {
    void sendNotification(NotificationRequest request);
    void scheduleNotification(NotificationRequest request);
}
