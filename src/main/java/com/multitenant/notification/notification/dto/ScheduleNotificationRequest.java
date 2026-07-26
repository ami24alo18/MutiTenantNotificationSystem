package com.multitenant.notification.notification.dto;

import com.multitenant.notification.channel.NotificationChannel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ScheduleNotificationRequest(
		@NotNull(message = "templateId is required")
		UUID templateId,

		@NotBlank(message = "recipient is required")
		@Size(max = 255, message = "recipient must be at most 255 characters")
		String recipient,

		@NotNull(message = "channel is required")
		NotificationChannel channel,

		Map<String, String> variables,

		@NotNull(message = "scheduledAt is required")
		@Future(message = "scheduledAt must be in the future")
		Instant scheduledAt,

		@Size(max = 100, message = "idempotencyKey must be at most 100 characters")
		String idempotencyKey,

		UUID tenantId
) {
}
