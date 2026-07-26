package com.multitenant.notification.tenant.dto;

import com.multitenant.notification.tenant.Tenant;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
		UUID id,
		String code,
		String name,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {

	public static TenantResponse from(Tenant tenant) {
		return new TenantResponse(
				tenant.getId(),
				tenant.getCode(),
				tenant.getName(),
				tenant.isActive(),
				tenant.getCreatedAt(),
				tenant.getUpdatedAt()
		);
	}
}
