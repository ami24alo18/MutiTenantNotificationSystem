package com.multitenant.notification.auth;

/**
 * Application roles for RBAC.
 */
public enum Role {

	PLATFORM_ADMIN,
	TENANT_ADMIN;

	public String authority() {
		return "ROLE_" + name();
	}
}
