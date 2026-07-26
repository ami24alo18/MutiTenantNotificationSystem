package com.multitenant.notification.auth.dto;

import com.multitenant.notification.auth.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserRequest(
		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,

		@NotBlank(message = "password is required")
		@Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
		String password,

		@NotBlank(message = "fullName is required")
		@Size(max = 255, message = "fullName must be at most 255 characters")
		String fullName,

		@NotNull(message = "role is required")
		Role role,

		UUID tenantId
) {
}
