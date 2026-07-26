package com.multitenant.notification.template.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PreviewTemplateRequest(
		@NotNull(message = "variables map is required")
		Map<String, String> variables
) {
}
