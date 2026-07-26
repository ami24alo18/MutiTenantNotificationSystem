package com.multitenant.notification.auth.dto;

import com.multitenant.notification.auth.Role;
import com.multitenant.notification.auth.UserAccount;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String email,
		String fullName,
		Role role,
		UUID tenantId,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {

	public static UserResponse from(UserAccount user) {
		UUID tenantId = user.getTenant() == null ? null : user.getTenant().getId();
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getFullName(),
				user.getRole(),
				tenantId,
				user.isActive(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
