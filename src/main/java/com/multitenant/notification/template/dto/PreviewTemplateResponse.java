package com.multitenant.notification.template.dto;

import java.util.List;

public record PreviewTemplateResponse(
		String subject,
		String body,
		List<String> variables
) {
}
