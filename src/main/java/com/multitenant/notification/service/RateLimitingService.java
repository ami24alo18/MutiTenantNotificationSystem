package com.multitenant.notification.service;

import com.google.common.util.concurrent.RateLimiter;
import com.multitenant.notification.config.RateLimitingConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {

    private final RateLimitingConfig rateLimitingConfig;
    private final Map<Long, RateLimiter> tenantRateLimiters = new ConcurrentHashMap<>();

    public RateLimitingService(RateLimitingConfig rateLimitingConfig) {
        this.rateLimitingConfig = rateLimitingConfig;
    }

    public boolean tryAcquire(Long tenantId) {
        if (!rateLimitingConfig.isEnabled()) {
            return true;
        }

        RateLimiter rateLimiter = tenantRateLimiters.computeIfAbsent(tenantId,
                id -> RateLimiter.create((double) rateLimitingConfig.getLimit() / rateLimitingConfig.getWindowInSeconds()));

        return rateLimiter.tryAcquire();
    }
}
