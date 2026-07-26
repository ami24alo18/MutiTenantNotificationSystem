package com.multitenant.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.retry")
public class RetryProperties {

	private int maxAttempts = 5;
	private long initialDelayMs = 1000;
	private long maxDelayMs = 60000;
	private long pollIntervalMs = 2000;

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public long getInitialDelayMs() {
		return initialDelayMs;
	}

	public void setInitialDelayMs(long initialDelayMs) {
		this.initialDelayMs = initialDelayMs;
	}

	public long getMaxDelayMs() {
		return maxDelayMs;
	}

	public void setMaxDelayMs(long maxDelayMs) {
		this.maxDelayMs = maxDelayMs;
	}

	public long getPollIntervalMs() {
		return pollIntervalMs;
	}

	public void setPollIntervalMs(long pollIntervalMs) {
		this.pollIntervalMs = pollIntervalMs;
	}
}
