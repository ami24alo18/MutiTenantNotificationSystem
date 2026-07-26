package com.multitenant.notification.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Authenticated principal carrying user identity, role, and optional tenant scope.
 */
public class UserPrincipal implements UserDetails {

	private final UUID id;
	private final String email;
	private final String passwordHash;
	private final Role role;
	private final UUID tenantId;
	private final boolean active;

	public UserPrincipal(UUID id, String email, String passwordHash, Role role, UUID tenantId, boolean active) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.tenantId = tenantId;
		this.active = active;
	}

	public static UserPrincipal from(UserAccount user) {
		UUID tenantId = user.getTenant() == null ? null : user.getTenant().getId();
		return new UserPrincipal(
				user.getId(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getRole(),
				tenantId,
				user.isActive()
		);
	}

	public UUID getId() {
		return id;
	}

	public Role getRole() {
		return role;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public boolean isPlatformAdmin() {
		return role == Role.PLATFORM_ADMIN;
	}

	public boolean isTenantAdmin() {
		return role == Role.TENANT_ADMIN;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.authority()));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return active;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
