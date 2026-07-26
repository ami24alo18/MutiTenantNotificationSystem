package com.multitenant.notification.tenant;

import com.multitenant.notification.auth.SecurityUtils;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.common.response.ApiResponse;
import com.multitenant.notification.tenant.dto.CreateTenantRequest;
import com.multitenant.notification.tenant.dto.TenantResponse;
import com.multitenant.notification.tenant.dto.UpdateTenantRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'TENANT_ADMIN')")
public class TenantController {

	private final TenantService tenantService;

	public TenantController(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('PLATFORM_ADMIN')")
	public ApiResponse<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("Tenant created", tenantService.create(request, actor));
	}

	@GetMapping
	public ApiResponse<List<TenantResponse>> list() {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(tenantService.list(actor));
	}

	@GetMapping("/{id}")
	public ApiResponse<TenantResponse> get(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(tenantService.getById(id, actor));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('PLATFORM_ADMIN')")
	public ApiResponse<TenantResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateTenantRequest request) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("Tenant updated", tenantService.update(id, request, actor));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('PLATFORM_ADMIN')")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		tenantService.delete(id, actor);
		return ApiResponse.okMessage("Tenant deactivated");
	}
}
