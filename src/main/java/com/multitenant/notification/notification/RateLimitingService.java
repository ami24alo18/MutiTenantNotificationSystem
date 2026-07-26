package com.multitenant.notification.notification;

import com.google.common.util.concurrent.RateLimiter;
import com.multitenant.notification.common.exception.ApiException;
import com.multitenant.notification.common.exception.ErrorCode;
import com.multitenant.notification.config.RateLimitingProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

	private final RateLimitingProperties properties;
	private final Map<UUID, RateLimiter> limiters = new ConcurrentHashMap<>();

	public RateLimitingService(RateLimitingProperties properties) {
		this.properties = properties;
	}

	public void acquireOrThrow(UUID tenantId) {
		if (!properties.isEnabled()) {
			return;
		}
		RateLimiter limiter = limiters.computeIfAbsent(
				tenantId,
				id -> RateLimiter.create(Math.max(1, properties.getPermitsPerSecond()))
		);
		if (!limiter.tryAcquire()) {
			throw new ApiException(
					ErrorCode.RATE_LIMITED,
					HttpStatus.TOO_MANY_REQUESTS,
					"Per-tenant rate limit exceeded"
			);
		}
	}
}
