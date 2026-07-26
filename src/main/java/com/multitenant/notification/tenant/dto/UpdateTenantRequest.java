package com.multitenant.notification.tenant.dto;

import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(
		@Size(max = 255, message = "name must be at most 255 characters")
		String name,

		Boolean active
) {
}
