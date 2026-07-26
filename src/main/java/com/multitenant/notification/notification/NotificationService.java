package com.multitenant.notification.notification;

import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.channel.ChannelConfiguration;
import com.multitenant.notification.channel.ChannelConfigurationRepository;
import com.multitenant.notification.channel.NotificationChannel;
import com.multitenant.notification.common.exception.ApiException;
import com.multitenant.notification.common.exception.ErrorCode;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.common.exception.ResourceNotFoundException;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import com.multitenant.notification.notification.dto.DeliveryResponse;
import com.multitenant.notification.notification.dto.ScheduleNotificationRequest;
import com.multitenant.notification.notification.dto.SendNotificationRequest;
import com.multitenant.notification.template.NotificationTemplate;
import com.multitenant.notification.template.NotificationTemplateRepository;
import com.multitenant.notification.template.TemplateVariableProcessor;
import com.multitenant.notification.tenant.Tenant;
import com.multitenant.notification.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

	private final DeliveryRepository deliveryRepository;
	private final NotificationTemplateRepository templateRepository;
	private final ChannelConfigurationRepository channelRepository;
	private final TenantRepository tenantRepository;
	private final TemplateVariableProcessor variableProcessor;
	private final RateLimitingService rateLimitingService;
	private final NotificationQueue notificationQueue;

	public NotificationService(
			DeliveryRepository deliveryRepository,
			NotificationTemplateRepository templateRepository,
			ChannelConfigurationRepository channelRepository,
			TenantRepository tenantRepository,
			TemplateVariableProcessor variableProcessor,
			RateLimitingService rateLimitingService,
			NotificationQueue notificationQueue
	) {
		this.deliveryRepository = deliveryRepository;
		this.templateRepository = templateRepository;
		this.channelRepository = channelRepository;
		this.tenantRepository = tenantRepository;
		this.variableProcessor = variableProcessor;
		this.rateLimitingService = rateLimitingService;
		this.notificationQueue = notificationQueue;
	}

	@Transactional
	public DeliveryResponse sendImmediate(SendNotificationRequest request, UserPrincipal actor) {
		UUID tenantId = resolveTenantId(request.tenantId(), actor);
		rateLimitingService.acquireOrThrow(tenantId);

		if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
			var existing = deliveryRepository.findByTenantIdAndIdempotencyKey(tenantId, request.idempotencyKey().trim());
			if (existing.isPresent()) {
				return DeliveryResponse.from(existing.get());
			}
		}

		PreparedContent prepared = prepareContent(tenantId, request.templateId(), request.channel(), request.variables());
		Delivery delivery = buildDelivery(
				tenantId,
				prepared,
				request.recipient().trim(),
				request.idempotencyKey(),
				DeliveryStatus.PENDING,
				null
		);

		try {
			delivery = deliveryRepository.save(delivery);
		}
		catch (RuntimeException ex) {
			if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
				return deliveryRepository.findByTenantIdAndIdempotencyKey(tenantId, request.idempotencyKey().trim())
						.map(DeliveryResponse::from)
						.orElseThrow(() -> ex);
			}
			throw ex;
		}

		notificationQueue.enqueue(delivery.getId());
		return DeliveryResponse.from(delivery);
	}

	@Transactional
	public DeliveryResponse schedule(ScheduleNotificationRequest request, UserPrincipal actor) {
		UUID tenantId = resolveTenantId(request.tenantId(), actor);
		rateLimitingService.acquireOrThrow(tenantId);

		if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
			var existing = deliveryRepository.findByTenantIdAndIdempotencyKey(tenantId, request.idempotencyKey().trim());
			if (existing.isPresent()) {
				return DeliveryResponse.from(existing.get());
			}
		}

		PreparedContent prepared = prepareContent(tenantId, request.templateId(), request.channel(), request.variables());
		Delivery delivery = buildDelivery(
				tenantId,
				prepared,
				request.recipient().trim(),
				request.idempotencyKey(),
				DeliveryStatus.SCHEDULED,
				request.scheduledAt()
		);
		return DeliveryResponse.from(deliveryRepository.save(delivery));
	}

	@Transactional(readOnly = true)
	public DeliveryResponse getById(UUID id, UserPrincipal actor) {
		Delivery delivery = findAccessible(id, actor);
		return DeliveryResponse.from(delivery);
	}

	private Delivery findAccessible(UUID id, UserPrincipal actor) {
		if (actor.isPlatformAdmin()) {
			return deliveryRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
		}
		return deliveryRepository.findByIdAndTenantId(id, actor.getTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
	}

	private PreparedContent prepareContent(
			UUID tenantId,
			UUID templateId,
			NotificationChannel channel,
			Map<String, String> variables
	) {
		Tenant tenant = tenantRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
		if (!tenant.isActive()) {
			throw new ForbiddenException("Tenant is inactive");
		}

		NotificationTemplate template = templateRepository.findByIdAndTenantId(templateId, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Template", templateId));
		if (!template.isActive()) {
			throw new ApiException(ErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, "Template is inactive");
		}
		if (template.getChannel() != channel) {
			throw new ApiException(
					ErrorCode.VALIDATION_FAILED,
					HttpStatus.BAD_REQUEST,
					"Request channel does not match template channel " + template.getChannel()
			);
		}

		ChannelConfiguration channelConfig = channelRepository.findByTenantIdAndChannel(tenantId, channel)
				.orElseThrow(() -> new ApiException(
						ErrorCode.VALIDATION_FAILED,
						HttpStatus.BAD_REQUEST,
						"Channel " + channel + " is not configured"
				));
		if (!channelConfig.isEnabled()) {
			throw new ApiException(
					ErrorCode.VALIDATION_FAILED,
					HttpStatus.BAD_REQUEST,
					"Channel " + channel + " is disabled for this tenant"
			);
		}

		Map<String, String> vars = variables == null ? Map.of() : variables;
		String subject = template.getSubject() == null ? null : variableProcessor.render(template.getSubject(), vars);
		String body = variableProcessor.render(template.getBody(), vars);
		return new PreparedContent(template.getId(), channel, subject, body);
	}

	private Delivery buildDelivery(
			UUID tenantId,
			PreparedContent prepared,
			String recipient,
			String idempotencyKey,
			DeliveryStatus status,
			java.time.Instant scheduledAt
	) {
		Delivery delivery = new Delivery();
		delivery.setTenantId(tenantId);
		delivery.setTemplateId(prepared.templateId());
		delivery.setChannel(prepared.channel());
		delivery.setRecipient(recipient);
		delivery.setSubject(prepared.subject());
		delivery.setContent(prepared.body());
		delivery.setStatus(status);
		delivery.setScheduledAt(scheduledAt);
		delivery.setRetryAttempts(0);
		if (idempotencyKey != null && !idempotencyKey.isBlank()) {
			delivery.setIdempotencyKey(idempotencyKey.trim());
		}
		return delivery;
	}

	private UUID resolveTenantId(UUID requestedTenantId, UserPrincipal actor) {
		if (actor.isTenantAdmin()) {
			if (requestedTenantId != null && !requestedTenantId.equals(actor.getTenantId())) {
				throw new ForbiddenException("Tenant admins can only send for their own tenant");
			}
			return actor.getTenantId();
		}
		if (!actor.isPlatformAdmin()) {
			throw new ForbiddenException("Insufficient permissions");
		}
		if (requestedTenantId == null) {
			throw new ApiException(ErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, "tenantId is required for platform admins");
		}
		return requestedTenantId;
	}

	private record PreparedContent(UUID templateId, NotificationChannel channel, String subject, String body) {
	}
}
