package com.multitenant.notification.channel;

import com.multitenant.notification.common.exception.ApiException;
import com.multitenant.notification.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encodes/decodes channel settings and validates required keys per channel when enabling.
 */
@Component
public class ChannelSettingsCodec {

	private static final Map<NotificationChannel, Set<String>> REQUIRED_WHEN_ENABLED = Map.of(
			NotificationChannel.EMAIL, Set.of("fromAddress"),
			NotificationChannel.SMS, Set.of("senderId"),
			NotificationChannel.PUSH, Set.of("appId"),
			NotificationChannel.IN_APP, Set.of("retentionDays")
	);

	public String encode(Map<String, String> settings) {
		if (settings == null || settings.isEmpty()) {
			return "{}";
		}
		return settings.entrySet().stream()
				.filter(e -> e.getKey() != null && !e.getKey().isBlank() && e.getValue() != null)
				.map(e -> "\"" + escape(e.getKey().trim()) + "\":\"" + escape(e.getValue().trim()) + "\"")
				.collect(Collectors.joining(",", "{", "}"));
	}

	public Map<String, String> decode(String json) {
		if (json == null || json.isBlank() || "{}".equals(json.trim())) {
			return Map.of();
		}
		String trimmed = json.trim();
		if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
			return Map.of();
		}
		String inner = trimmed.substring(1, trimmed.length() - 1).trim();
		if (inner.isEmpty()) {
			return Map.of();
		}

		Map<String, String> result = new LinkedHashMap<>();
		for (String part : inner.split(",")) {
			String[] kv = part.split(":", 2);
			if (kv.length != 2) {
				continue;
			}
			result.put(unquote(kv[0].trim()), unquote(kv[1].trim()));
		}
		return Collections.unmodifiableMap(result);
	}

	public void validateForEnable(NotificationChannel channel, boolean enabled, Map<String, String> settings) {
		if (!enabled) {
			return;
		}
		Map<String, String> effective = settings == null ? Map.of() : settings;
		for (String required : REQUIRED_WHEN_ENABLED.get(channel)) {
			String value = effective.get(required);
			if (value == null || value.isBlank()) {
				throw new ApiException(
						ErrorCode.VALIDATION_FAILED,
						HttpStatus.BAD_REQUEST,
						"settings." + required + " is required to enable " + channel
				);
			}
		}
		if (channel == NotificationChannel.IN_APP) {
			try {
				int days = Integer.parseInt(effective.get("retentionDays").trim());
				if (days < 1 || days > 3650) {
					throw new ApiException(
							ErrorCode.VALIDATION_FAILED,
							HttpStatus.BAD_REQUEST,
							"settings.retentionDays must be between 1 and 3650"
					);
				}
			}
			catch (NumberFormatException ex) {
				throw new ApiException(
						ErrorCode.VALIDATION_FAILED,
						HttpStatus.BAD_REQUEST,
						"settings.retentionDays must be a number"
				);
			}
		}
		if (channel == NotificationChannel.EMAIL) {
			String from = effective.get("fromAddress");
			if (!from.contains("@")) {
				throw new ApiException(
						ErrorCode.VALIDATION_FAILED,
						HttpStatus.BAD_REQUEST,
						"settings.fromAddress must be a valid email-like address"
				);
			}
		}
	}

	private static String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static String unquote(String token) {
		if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
			return token.substring(1, token.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
		}
		return token;
	}
}
