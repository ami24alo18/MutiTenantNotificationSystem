package com.multitenant.notification.template.dto;

import com.multitenant.notification.template.NotificationTemplate;
import com.multitenant.notification.template.TemplateChannel;
import com.multitenant.notification.template.TemplateVariableProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TemplateResponse(
		UUID id,
		UUID tenantId,
		String code,
		String name,
		TemplateChannel channel,
		String subject,
		String body,
		List<String> variables,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {

	public static TemplateResponse from(NotificationTemplate template, TemplateVariableProcessor processor) {
		return new TemplateResponse(
				template.getId(),
				template.getTenant().getId(),
				template.getCode(),
				template.getName(),
				template.getChannel(),
				template.getSubject(),
				template.getBody(),
				processor.fromJsonArray(template.getVariables()),
				template.isActive(),
				template.getCreatedAt(),
				template.getUpdatedAt()
		);
	}
}
