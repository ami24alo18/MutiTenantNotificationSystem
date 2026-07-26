package com.multitenant.notification.tenant;

import com.multitenant.notification.auth.Role;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.common.exception.ConflictException;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.tenant.dto.CreateTenantRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

	@Mock
	private TenantRepository tenantRepository;

	@InjectMocks
	private TenantService tenantService;

	@Test
	void createRejectsNonPlatformAdmin() {
		UserPrincipal tenantAdmin = new UserPrincipal(
				UUID.randomUUID(),
				"admin@acme.test",
				"hash",
				Role.TENANT_ADMIN,
				UUID.randomUUID(),
				true
		);

		assertThatThrownBy(() -> tenantService.create(new CreateTenantRequest("acme", "Acme"), tenantAdmin))
				.isInstanceOf(ForbiddenException.class);

		verify(tenantRepository, never()).save(any());
	}

	@Test
	void createRejectsDuplicateCode() {
		UserPrincipal platformAdmin = new UserPrincipal(
				UUID.randomUUID(),
				"platform.admin@system.local",
				"hash",
				Role.PLATFORM_ADMIN,
				null,
				true
		);
		when(tenantRepository.existsByCodeIgnoreCase("acme")).thenReturn(true);

		assertThatThrownBy(() -> tenantService.create(new CreateTenantRequest("acme", "Acme"), platformAdmin))
				.isInstanceOf(ConflictException.class);
	}
}
