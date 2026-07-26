package com.multitenant.notification.template;

import com.multitenant.notification.auth.Role;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.template.dto.CreateTemplateRequest;
import com.multitenant.notification.tenant.TenantRepository;
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

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

	@Mock
	private NotificationTemplateRepository templateRepository;

	@Mock
	private TenantRepository tenantRepository;

	@Mock
	private TemplateVariableProcessor variableProcessor;

	@InjectMocks
	private NotificationTemplateService templateService;

	@Test
	void tenantAdminCannotCreateTemplateForAnotherTenant() {
		UUID ownTenantId = UUID.randomUUID();
		UUID otherTenantId = UUID.randomUUID();
		UserPrincipal actor = new UserPrincipal(
				UUID.randomUUID(),
				"admin@acme.test",
				"hash",
				Role.TENANT_ADMIN,
				ownTenantId,
				true
		);

		CreateTemplateRequest request = new CreateTemplateRequest(
				"welcome-email",
				"Welcome",
				TemplateChannel.EMAIL,
				"Hello",
				"Body",
				otherTenantId
		);

		assertThatThrownBy(() -> templateService.create(request, actor))
				.isInstanceOf(ForbiddenException.class);

		verify(templateRepository, never()).save(any());
	}
}
