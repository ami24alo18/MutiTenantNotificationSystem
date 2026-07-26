package com.multitenant.notification.notification.dto;

import com.multitenant.notification.channel.NotificationChannel;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryStatus;

import java.time.Instant;
import java.util.UUID;

public record DeliveryResponse(
		UUID id,
		UUID tenantId,
		UUID templateId,
		NotificationChannel channel,
		String recipient,
		String subject,
		String content,
		DeliveryStatus status,
		String idempotencyKey,
		Instant scheduledAt,
		Instant sentAt,
		int retryAttempts,
		Instant nextRetryAt,
		String lastError,
		Instant createdAt,
		Instant updatedAt
) {

	public static DeliveryResponse from(Delivery delivery) {
		return new DeliveryResponse(
				delivery.getId(),
				delivery.getTenantId(),
				delivery.getTemplateId(),
				delivery.getChannel(),
				delivery.getRecipient(),
				delivery.getSubject(),
				delivery.getContent(),
				delivery.getStatus(),
				delivery.getIdempotencyKey(),
				delivery.getScheduledAt(),
				delivery.getSentAt(),
				delivery.getRetryAttempts(),
				delivery.getNextRetryAt(),
				delivery.getLastError(),
				delivery.getCreatedAt(),
				delivery.getUpdatedAt()
		);
	}
}
