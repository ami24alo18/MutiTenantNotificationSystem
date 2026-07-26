package com.multitenant.notification.auth;

import com.multitenant.notification.auth.dto.CreateUserRequest;
import com.multitenant.notification.auth.dto.UpdateUserRequest;
import com.multitenant.notification.auth.dto.UserResponse;
import com.multitenant.notification.common.exception.ConflictException;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.common.exception.ResourceNotFoundException;
import com.multitenant.notification.tenant.Tenant;
import com.multitenant.notification.tenant.TenantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserAccountService {

	private final UserAccountRepository userAccountRepository;
	private final TenantRepository tenantRepository;
	private final PasswordEncoder passwordEncoder;

	public UserAccountService(
			UserAccountRepository userAccountRepository,
			TenantRepository tenantRepository,
			PasswordEncoder passwordEncoder
	) {
		this.userAccountRepository = userAccountRepository;
		this.tenantRepository = tenantRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserResponse create(CreateUserRequest request, UserPrincipal actor) {
		validateCreatePermissions(request, actor);

		String email = normalizeEmail(request.email());
		if (userAccountRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("User already exists with email: " + email);
		}

		Tenant tenant = resolveTenantForCreate(request, actor);

		UserAccount user = new UserAccount();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setFullName(request.fullName().trim());
		user.setRole(request.role());
		user.setTenant(tenant);
		user.setActive(true);

		return UserResponse.from(userAccountRepository.save(user));
	}

	@Transactional(readOnly = true)
	public List<UserResponse> list(UserPrincipal actor) {
		if (actor.isPlatformAdmin()) {
			return userAccountRepository.findAll().stream().map(UserResponse::from).toList();
		}
		return userAccountRepository.findByTenantId(actor.getTenantId()).stream()
				.map(UserResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public UserResponse getById(UUID id, UserPrincipal actor) {
		UserAccount user = findAccessibleUser(id, actor);
		return UserResponse.from(user);
	}

	@Transactional
	public UserResponse update(UUID id, UpdateUserRequest request, UserPrincipal actor) {
		UserAccount user = findAccessibleUser(id, actor);

		if (request.fullName() != null && !request.fullName().isBlank()) {
			user.setFullName(request.fullName().trim());
		}
		if (request.password() != null && !request.password().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(request.password()));
		}
		if (request.active() != null) {
			if (actor.getId().equals(user.getId()) && Boolean.FALSE.equals(request.active())) {
				throw new ForbiddenException("Users cannot deactivate themselves");
			}
			user.setActive(request.active());
		}

		return UserResponse.from(userAccountRepository.save(user));
	}

	@Transactional
	public void delete(UUID id, UserPrincipal actor) {
		UserAccount user = findAccessibleUser(id, actor);
		if (actor.getId().equals(user.getId())) {
			throw new ForbiddenException("Users cannot delete themselves");
		}
		user.setActive(false);
		userAccountRepository.save(user);
	}

	private UserAccount findAccessibleUser(UUID id, UserPrincipal actor) {
		UserAccount user = userAccountRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User", id));

		if (actor.isPlatformAdmin()) {
			return user;
		}

		if (user.getTenant() == null || !user.getTenant().getId().equals(actor.getTenantId())) {
			throw new ForbiddenException("Cannot access users outside your tenant");
		}
		return user;
	}

	private void validateCreatePermissions(CreateUserRequest request, UserPrincipal actor) {
		if (actor.isPlatformAdmin()) {
			if (request.role() == Role.PLATFORM_ADMIN && request.tenantId() != null) {
				throw new ForbiddenException("Platform admin must not be associated with a tenant");
			}
			if (request.role() == Role.TENANT_ADMIN && request.tenantId() == null) {
				throw new ForbiddenException("Tenant admin requires a tenantId");
			}
			return;
		}

		if (!actor.isTenantAdmin()) {
			throw new ForbiddenException("Insufficient permissions to create users");
		}
		if (request.role() != Role.TENANT_ADMIN) {
			throw new ForbiddenException("Tenant admins can only create tenant admin users");
		}
		if (request.tenantId() != null && !request.tenantId().equals(actor.getTenantId())) {
			throw new ForbiddenException("Tenant admins can only create users in their own tenant");
		}
	}

	private Tenant resolveTenantForCreate(CreateUserRequest request, UserPrincipal actor) {
		if (request.role() == Role.PLATFORM_ADMIN) {
			return null;
		}

		UUID tenantId = actor.isTenantAdmin() ? actor.getTenantId() : request.tenantId();
		Tenant tenant = tenantRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
		if (!tenant.isActive()) {
			throw new ForbiddenException("Cannot assign users to an inactive tenant");
		}
		return tenant;
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
