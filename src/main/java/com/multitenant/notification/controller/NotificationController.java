package com.multitenant.notification.controller;

import com.multitenant.notification.dto.NotificationRequest;
import com.multitenant.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.sendNotification(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/schedule")
    public ResponseEntity<Void> scheduleNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.scheduleNotification(request);
        return ResponseEntity.accepted().build();
    }
}
