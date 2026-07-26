package com.multitenant.notification.template.dto;

import com.multitenant.notification.channel.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTemplateRequest(
		@NotBlank(message = "code is required")
		@Size(min = 2, max = 100, message = "code must be between 2 and 100 characters")
		@Pattern(
				regexp = "^[a-z0-9][a-z0-9_-]*$",
				message = "code must be lowercase alphanumeric with optional _ or -"
		)
		String code,

		@NotBlank(message = "name is required")
		@Size(max = 255, message = "name must be at most 255 characters")
		String name,

		@NotNull(message = "channel is required")
		NotificationChannel channel,

		@Size(max = 500, message = "subject must be at most 500 characters")
		String subject,

		@NotBlank(message = "body is required")
		@Size(max = 20000, message = "body must be at most 20000 characters")
		String body,

		/**
		 * Required for platform admins. Ignored for tenant admins (forced to their tenant).
		 */
		UUID tenantId
) {
}
