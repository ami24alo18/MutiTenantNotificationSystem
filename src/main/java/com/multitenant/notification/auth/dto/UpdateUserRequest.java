package com.multitenant.notification.auth.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
		@Size(max = 255, message = "fullName must be at most 255 characters")
		String fullName,

		@Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
		String password,

		Boolean active
) {
}
