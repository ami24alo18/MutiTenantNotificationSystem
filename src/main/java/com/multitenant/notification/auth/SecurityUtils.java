package com.multitenant.notification.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static UserPrincipal requireCurrentUser() {
		return currentUser()
				.orElseThrow(() -> new IllegalStateException("No authenticated user in security context"));
	}

	public static Optional<UserPrincipal> currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			return Optional.empty();
		}
		return Optional.of(principal);
	}
}
