package com.multitenant.notification.channel.dto;

import jakarta.validation.constraints.NotNull;

public record ToggleChannelRequest(
		@NotNull(message = "enabled is required")
		Boolean enabled
) {
}
