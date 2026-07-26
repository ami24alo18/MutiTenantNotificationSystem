package com.multitenant.notification.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

	private final JwtProperties properties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(UserAccount user) {
		Instant now = Instant.now();
		Instant expiry = now.plusMillis(properties.getExpirationMs());

		var builder = Jwts.builder()
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("role", user.getRole().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry));

		if (user.getTenant() != null) {
			builder.claim("tenantId", user.getTenant().getId().toString());
		}

		return builder.signWith(secretKey).compact();
	}

	public Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		}
		catch (JwtException | IllegalArgumentException ex) {
			throw new UnauthorizedTokenException("Invalid or expired JWT token", ex);
		}
	}

	public UUID extractUserId(Claims claims) {
		return UUID.fromString(claims.getSubject());
	}

	public long getExpirationMs() {
		return properties.getExpirationMs();
	}

	static final class UnauthorizedTokenException extends RuntimeException {
		UnauthorizedTokenException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
