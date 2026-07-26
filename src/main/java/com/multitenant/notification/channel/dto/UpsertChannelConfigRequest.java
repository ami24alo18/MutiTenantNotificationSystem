package com.multitenant.notification.channel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpsertChannelConfigRequest(
		@NotNull(message = "enabled is required")
		Boolean enabled,

		@Size(max = 100, message = "provider must be at most 100 characters")
		String provider,

		Map<String, String> settings
) {
}
