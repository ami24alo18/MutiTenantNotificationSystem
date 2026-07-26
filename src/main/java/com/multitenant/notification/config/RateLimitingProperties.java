package com.multitenant.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limiting")
public class RateLimitingProperties {

	private boolean enabled = true;
	private int permitsPerSecond = 10;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getPermitsPerSecond() {
		return permitsPerSecond;
	}

	public void setPermitsPerSecond(int permitsPerSecond) {
		this.permitsPerSecond = permitsPerSecond;
	}
}
