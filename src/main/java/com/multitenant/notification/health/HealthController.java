package com.multitenant.notification.health;

import com.multitenant.notification.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight application health probe for Phase 1.
 * Actuator {@code /actuator/health} remains available for infrastructure checks.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

	@GetMapping
	public ApiResponse<Map<String, String>> health() {
		return ApiResponse.ok(Map.of(
				"status", "UP",
				"service", "multi-tenant-notification-system"
		));
	}
}
