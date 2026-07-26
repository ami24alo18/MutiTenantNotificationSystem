package com.multitenant.notification.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	/**
	 * HMAC secret used to sign JWTs. Must be at least 32 characters.
	 */
	private String secret;

	/**
	 * Access token lifetime in milliseconds.
	 */
	private long expirationMs = 3_600_000L;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	public void setExpirationMs(long expirationMs) {
		this.expirationMs = expirationMs;
	}
}
