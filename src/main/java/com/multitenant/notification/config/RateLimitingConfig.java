package com.multitenant.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "notification.rate-limiting")
public class RateLimitingConfig {

    private boolean enabled;
    private int limit;
    private int windowInSeconds;
}
