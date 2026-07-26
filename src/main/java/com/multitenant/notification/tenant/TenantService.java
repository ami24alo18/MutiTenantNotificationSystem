package com.multitenant.notification.tenant;

import com.multitenant.notification.auth.Role;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.common.exception.ConflictException;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.common.exception.ResourceNotFoundException;
import com.multitenant.notification.tenant.dto.CreateTenantRequest;
import com.multitenant.notification.tenant.dto.TenantResponse;
import com.multitenant.notification.tenant.dto.UpdateTenantRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TenantService {

	private final TenantRepository tenantRepository;

	public TenantService(TenantRepository tenantRepository) {
		this.tenantRepository = tenantRepository;
	}

	@Transactional
	public TenantResponse create(CreateTenantRequest request, UserPrincipal actor) {
		requirePlatformAdmin(actor);

		String code = request.code().trim().toLowerCase(Locale.ROOT);
		if (tenantRepository.existsByCodeIgnoreCase(code)) {
			throw new ConflictException("Tenant already exists with code: " + code);
		}

		Tenant tenant = new Tenant();
		tenant.setCode(code);
		tenant.setName(request.name().trim());
		tenant.setActive(true);
		return TenantResponse.from(tenantRepository.save(tenant));
	}

	@Transactional(readOnly = true)
	public List<TenantResponse> list(UserPrincipal actor) {
		if (actor.isPlatformAdmin()) {
			return tenantRepository.findAll().stream().map(TenantResponse::from).toList();
		}
		Tenant tenant = tenantRepository.findById(actor.getTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", actor.getTenantId()));
		return List.of(TenantResponse.from(tenant));
	}

	@Transactional(readOnly = true)
	public TenantResponse getById(UUID id, UserPrincipal actor) {
		Tenant tenant = findAccessibleTenant(id, actor);
		return TenantResponse.from(tenant);
	}

	@Transactional
	public TenantResponse update(UUID id, UpdateTenantRequest request, UserPrincipal actor) {
		requirePlatformAdmin(actor);
		Tenant tenant = tenantRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", id));

		if (request.name() != null && !request.name().isBlank()) {
			tenant.setName(request.name().trim());
		}
		if (request.active() != null) {
			tenant.setActive(request.active());
		}
		return TenantResponse.from(tenantRepository.save(tenant));
	}

	@Transactional
	public void delete(UUID id, UserPrincipal actor) {
		requirePlatformAdmin(actor);
		Tenant tenant = tenantRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
		tenant.setActive(false);
		tenantRepository.save(tenant);
	}

	private Tenant findAccessibleTenant(UUID id, UserPrincipal actor) {
		Tenant tenant = tenantRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", id));

		if (actor.isPlatformAdmin()) {
			return tenant;
		}
		if (actor.getRole() == Role.TENANT_ADMIN && id.equals(actor.getTenantId())) {
			return tenant;
		}
		throw new ForbiddenException("Cannot access tenants outside your scope");
	}

	private static void requirePlatformAdmin(UserPrincipal actor) {
		if (!actor.isPlatformAdmin()) {
			throw new ForbiddenException("Only platform admins can manage tenants");
		}
	}
}
