package com.multitenant.notification.auth.dto;

public record LoginResponse(
		String accessToken,
		String tokenType,
		long expiresInMs,
		UserResponse user
) {
}
