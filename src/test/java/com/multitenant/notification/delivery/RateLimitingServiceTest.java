package com.multitenant.notification.delivery;

import com.multitenant.notification.config.RateLimitingConfig;
import com.multitenant.notification.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;
    private RateLimitingConfig rateLimitingConfig;

    @BeforeEach
    void setUp() {
        rateLimitingConfig = new RateLimitingConfig();
        rateLimitingConfig.setEnabled(true);
        rateLimitingConfig.setLimit(2);
        rateLimitingConfig.setWindowInSeconds(1);
        rateLimitingService = new RateLimitingService(rateLimitingConfig);
    }

    @Test
    void whenLimitExceeded_tryAcquireShouldFail() {
        Long tenantId = 1L;
        assertTrue(rateLimitingService.tryAcquire(tenantId));
        assertTrue(rateLimitingService.tryAcquire(tenantId));
        assertFalse(rateLimitingService.tryAcquire(tenantId));
    }

    @Test
    void whenRateLimitingDisabled_tryAcquireShouldAlwaysSucceed() {
        rateLimitingConfig.setEnabled(false);
        Long tenantId = 2L;
        assertTrue(rateLimitingService.tryAcquire(tenantId));
        assertTrue(rateLimitingService.tryAcquire(tenantId));
        assertTrue(rateLimitingService.tryAcquire(tenantId));
    }
}
