package com.multitenant.notification.template.dto;

import jakarta.validation.constraints.Size;

public record UpdateTemplateRequest(
		@Size(max = 255, message = "name must be at most 255 characters")
		String name,

		@Size(max = 500, message = "subject must be at most 500 characters")
		String subject,

		@Size(max = 20000, message = "body must be at most 20000 characters")
		String body,

		Boolean active
) {
}
