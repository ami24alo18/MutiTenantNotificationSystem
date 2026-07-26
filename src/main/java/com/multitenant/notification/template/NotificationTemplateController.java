package com.multitenant.notification.template;

import com.multitenant.notification.auth.SecurityUtils;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.common.response.ApiResponse;
import com.multitenant.notification.template.dto.CreateTemplateRequest;
import com.multitenant.notification.template.dto.PreviewTemplateRequest;
import com.multitenant.notification.template.dto.PreviewTemplateResponse;
import com.multitenant.notification.template.dto.TemplateResponse;
import com.multitenant.notification.template.dto.UpdateTemplateRequest;
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
@RequestMapping("/api/v1/templates")
@PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'TENANT_ADMIN')")
public class NotificationTemplateController {

	private final NotificationTemplateService templateService;

	public NotificationTemplateController(NotificationTemplateService templateService) {
		this.templateService = templateService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<TemplateResponse> create(@Valid @RequestBody CreateTemplateRequest request) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("Template created", templateService.create(request, actor));
	}

	@GetMapping
	public ApiResponse<List<TemplateResponse>> list() {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(templateService.list(actor));
	}

	@GetMapping("/{id}")
	public ApiResponse<TemplateResponse> get(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(templateService.getById(id, actor));
	}

	@PutMapping("/{id}")
	public ApiResponse<TemplateResponse> update(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateTemplateRequest request
	) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("Template updated", templateService.update(id, request, actor));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		templateService.delete(id, actor);
		return ApiResponse.okMessage("Template deactivated");
	}

	@PostMapping("/{id}/preview")
	public ApiResponse<PreviewTemplateResponse> preview(
			@PathVariable UUID id,
			@Valid @RequestBody PreviewTemplateRequest request
	) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(templateService.preview(id, request, actor));
	}
}
