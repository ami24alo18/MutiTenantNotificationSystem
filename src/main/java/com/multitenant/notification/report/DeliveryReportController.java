package com.multitenant.notification.report;

import com.multitenant.notification.auth.SecurityUtils;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.common.exception.ApiException;
import com.multitenant.notification.common.exception.ErrorCode;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.common.exception.ResourceNotFoundException;
import com.multitenant.notification.common.response.ApiResponse;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryAttemptRepository;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import com.multitenant.notification.notification.dto.DeliveryAttemptResponse;
import com.multitenant.notification.notification.dto.DeliveryResponse;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'TENANT_ADMIN')")
public class DeliveryReportController {

	private final DeliveryRepository deliveryRepository;
	private final DeliveryAttemptRepository attemptRepository;

	public DeliveryReportController(
			DeliveryRepository deliveryRepository,
			DeliveryAttemptRepository attemptRepository
	) {
		this.deliveryRepository = deliveryRepository;
		this.attemptRepository = attemptRepository;
	}

	@GetMapping("/deliveries")
	public ApiResponse<List<DeliveryResponse>> search(
			@RequestParam(required = false) UUID tenantId,
			@RequestParam(required = false) DeliveryStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
	) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		UUID scopedTenantId = resolveTenantScope(tenantId, actor);

		Specification<Delivery> spec = (root, query, cb) -> cb.conjunction();
		if (scopedTenantId != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), scopedTenantId));
		}
		if (status != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
		}
		if (from != null) {
			spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
		}
		if (to != null) {
			spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
		}

		List<DeliveryResponse> results = deliveryRepository.findAll(spec).stream()
				.map(DeliveryResponse::from)
				.toList();
		return ApiResponse.ok(results);
	}

	@GetMapping("/deliveries/{id}")
	public ApiResponse<DeliveryResponse> getDelivery(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(DeliveryResponse.from(findAccessibleDelivery(id, actor)));
	}

	@GetMapping("/deliveries/{id}/attempts")
	public ApiResponse<List<DeliveryAttemptResponse>> getAttempts(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		Delivery delivery = findAccessibleDelivery(id, actor);
		List<DeliveryAttemptResponse> attempts = attemptRepository
				.findByDeliveryIdOrderByAttemptNumberAsc(delivery.getId())
				.stream()
				.map(DeliveryAttemptResponse::from)
				.toList();
		return ApiResponse.ok(attempts);
	}

	private Delivery findAccessibleDelivery(UUID id, UserPrincipal actor) {
		if (actor.isPlatformAdmin()) {
			return deliveryRepository.findById(id)
					.orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
		}
		return deliveryRepository.findByIdAndTenantId(id, actor.getTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
	}

	private UUID resolveTenantScope(UUID requestedTenantId, UserPrincipal actor) {
		if (actor.isTenantAdmin()) {
			if (requestedTenantId != null && !requestedTenantId.equals(actor.getTenantId())) {
				throw new ForbiddenException("Cannot view other tenants' delivery reports");
			}
			return actor.getTenantId();
		}
		if (actor.isPlatformAdmin()) {
			return requestedTenantId;
		}
		throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Insufficient permissions");
	}
}
