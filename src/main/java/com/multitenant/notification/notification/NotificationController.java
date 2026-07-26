package com.multitenant.notification.notification;

import com.multitenant.notification.auth.SecurityUtils;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.common.response.ApiResponse;
import com.multitenant.notification.notification.dto.DeliveryResponse;
import com.multitenant.notification.notification.dto.ScheduleNotificationRequest;
import com.multitenant.notification.notification.dto.SendNotificationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'TENANT_ADMIN')")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@PostMapping("/send")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResponse<DeliveryResponse> send(@Valid @RequestBody SendNotificationRequest request) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("Notification accepted", notificationService.sendImmediate(request, actor));
	}

	@PostMapping("/schedule")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResponse<DeliveryResponse> schedule(@Valid @RequestBody ScheduleNotificationRequest request) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("Notification scheduled", notificationService.schedule(request, actor));
	}

	@GetMapping("/{id}")
	public ApiResponse<DeliveryResponse> get(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(notificationService.getById(id, actor));
	}
}
