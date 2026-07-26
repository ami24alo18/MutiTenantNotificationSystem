package com.multitenant.notification.template;

import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.channel.NotificationChannel;
import com.multitenant.notification.common.exception.ApiException;
import com.multitenant.notification.common.exception.ConflictException;
import com.multitenant.notification.common.exception.ErrorCode;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.common.exception.ResourceNotFoundException;
import com.multitenant.notification.template.dto.CreateTemplateRequest;
import com.multitenant.notification.template.dto.PreviewTemplateRequest;
import com.multitenant.notification.template.dto.PreviewTemplateResponse;
import com.multitenant.notification.template.dto.TemplateResponse;
import com.multitenant.notification.template.dto.UpdateTemplateRequest;
import com.multitenant.notification.tenant.Tenant;
import com.multitenant.notification.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class NotificationTemplateService {

	private final NotificationTemplateRepository templateRepository;
	private final TenantRepository tenantRepository;
	private final TemplateVariableProcessor variableProcessor;

	public NotificationTemplateService(
			NotificationTemplateRepository templateRepository,
			TenantRepository tenantRepository,
			TemplateVariableProcessor variableProcessor
	) {
		this.templateRepository = templateRepository;
		this.tenantRepository = tenantRepository;
		this.variableProcessor = variableProcessor;
	}

	@Transactional
	public TemplateResponse create(CreateTemplateRequest request, UserPrincipal actor) {
		Tenant tenant = resolveTenantForWrite(request.tenantId(), actor);
		String code = request.code().trim().toLowerCase(Locale.ROOT);

		if (templateRepository.existsByTenantIdAndCodeIgnoreCase(tenant.getId(), code)) {
			throw new ConflictException("Template already exists with code: " + code);
		}

		validateChannelContent(request.channel(), request.subject(), request.body());
		List<String> variables = variableProcessor.extractVariables(request.subject(), request.body());

		NotificationTemplate template = new NotificationTemplate();
		template.setTenant(tenant);
		template.setCode(code);
		template.setName(request.name().trim());
		template.setChannel(request.channel());
		template.setSubject(normalizeSubject(request.channel(), request.subject()));
		template.setBody(request.body());
		template.setVariables(variableProcessor.toJsonArray(variables));
		template.setActive(true);

		return TemplateResponse.from(templateRepository.save(template), variableProcessor);
	}

	@Transactional(readOnly = true)
	public List<TemplateResponse> list(UserPrincipal actor) {
		List<NotificationTemplate> templates = actor.isPlatformAdmin()
				? templateRepository.findAllByOrderByCreatedAtDesc()
				: templateRepository.findByTenantIdOrderByCreatedAtDesc(actor.getTenantId());

		return templates.stream()
				.map(template -> TemplateResponse.from(template, variableProcessor))
				.toList();
	}

	@Transactional(readOnly = true)
	public TemplateResponse getById(UUID id, UserPrincipal actor) {
		return TemplateResponse.from(findAccessibleTemplate(id, actor), variableProcessor);
	}

	@Transactional
	public TemplateResponse update(UUID id, UpdateTemplateRequest request, UserPrincipal actor) {
		NotificationTemplate template = findAccessibleTemplate(id, actor);

		if (request.name() != null && !request.name().isBlank()) {
			template.setName(request.name().trim());
		}

		boolean contentChanged = false;
		String subject = template.getSubject();
		String body = template.getBody();

		if (request.subject() != null) {
			subject = request.subject();
			contentChanged = true;
		}
		if (request.body() != null && !request.body().isBlank()) {
			body = request.body();
			contentChanged = true;
		}

		if (contentChanged) {
			validateChannelContent(template.getChannel(), subject, body);
			List<String> variables = variableProcessor.extractVariables(subject, body);
			template.setSubject(normalizeSubject(template.getChannel(), subject));
			template.setBody(body);
			template.setVariables(variableProcessor.toJsonArray(variables));
		}

		if (request.active() != null) {
			template.setActive(request.active());
		}

		return TemplateResponse.from(templateRepository.save(template), variableProcessor);
	}

	@Transactional
	public void delete(UUID id, UserPrincipal actor) {
		NotificationTemplate template = findAccessibleTemplate(id, actor);
		template.setActive(false);
		templateRepository.save(template);
	}

	@Transactional(readOnly = true)
	public PreviewTemplateResponse preview(UUID id, PreviewTemplateRequest request, UserPrincipal actor) {
		NotificationTemplate template = findAccessibleTemplate(id, actor);
		String renderedSubject = template.getSubject() == null
				? null
				: variableProcessor.render(template.getSubject(), request.variables());
		String renderedBody = variableProcessor.render(template.getBody(), request.variables());
		return new PreviewTemplateResponse(
				renderedSubject,
				renderedBody,
				variableProcessor.fromJsonArray(template.getVariables())
		);
	}

	private NotificationTemplate findAccessibleTemplate(UUID id, UserPrincipal actor) {
		if (actor.isPlatformAdmin()) {
			return templateRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Template", id));
		}

		return templateRepository.findByIdAndTenantId(id, actor.getTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Template", id));
	}

	private Tenant resolveTenantForWrite(UUID requestedTenantId, UserPrincipal actor) {
		if (actor.isTenantAdmin()) {
			if (requestedTenantId != null && !requestedTenantId.equals(actor.getTenantId())) {
				throw new ForbiddenException("Tenant admins can only manage templates in their own tenant");
			}
			return tenantRepository.findById(actor.getTenantId())
					.orElseThrow(() -> new ResourceNotFoundException("Tenant", actor.getTenantId()));
		}

		if (!actor.isPlatformAdmin()) {
			throw new ForbiddenException("Insufficient permissions to manage templates");
		}
		if (requestedTenantId == null) {
			throw new ApiException(
					ErrorCode.VALIDATION_FAILED,
					HttpStatus.BAD_REQUEST,
					"tenantId is required for platform admins"
			);
		}

		Tenant tenant = tenantRepository.findById(requestedTenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", requestedTenantId));
		if (!tenant.isActive()) {
			throw new ForbiddenException("Cannot create templates for an inactive tenant");
		}
		return tenant;
	}

	private static void validateChannelContent(NotificationChannel channel, String subject, String body) {
		if (body == null || body.isBlank()) {
			throw new ApiException(
					ErrorCode.VALIDATION_FAILED,
					HttpStatus.BAD_REQUEST,
					"body is required"
			);
		}
		if (channel == NotificationChannel.EMAIL && (subject == null || subject.isBlank())) {
			throw new ApiException(
					ErrorCode.VALIDATION_FAILED,
					HttpStatus.BAD_REQUEST,
					"subject is required for EMAIL templates"
			);
		}
	}

	private static String normalizeSubject(NotificationChannel channel, String subject) {
		if (channel != NotificationChannel.EMAIL) {
			return (subject == null || subject.isBlank()) ? null : subject.trim();
		}
		return subject.trim();
	}
}
