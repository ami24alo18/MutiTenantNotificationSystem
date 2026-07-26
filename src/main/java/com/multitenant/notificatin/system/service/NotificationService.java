package com.multitenant.notificatin.system.service;

import com.multitenant.notificatin.system.dto.NotificationRequest;

public interface NotificationService {
    void sendNotification(NotificationRequest request);
    void scheduleNotification(NotificationRequest request);
}
