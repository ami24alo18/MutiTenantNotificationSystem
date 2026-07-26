package com.multitenant.notification.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
		@NotBlank(message = "code is required")
		@Size(min = 2, max = 64, message = "code must be between 2 and 64 characters")
		@Pattern(regexp = "^[a-z0-9][a-z0-9_-]*$", message = "code must be lowercase alphanumeric with optional _ or -")
		String code,

		@NotBlank(message = "name is required")
		@Size(max = 255, message = "name must be at most 255 characters")
		String name
) {
}
