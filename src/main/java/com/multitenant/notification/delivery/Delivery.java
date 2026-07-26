package com.multitenant.notification.delivery;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;

    private String channel;

    private String recipient;

    private String content;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    private int retryAttempts;

    private LocalDateTime nextRetryAt;
}
