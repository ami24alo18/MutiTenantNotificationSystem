package com.multitenant.notification.template;

import com.multitenant.notification.common.exception.ApiException;
import com.multitenant.notification.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extracts and substitutes {@code {{variableName}}} placeholders in templates.
 */
@Component
public class TemplateVariableProcessor {

	private static final Pattern PLACEHOLDER_PATTERN =
			Pattern.compile("\\{\\{\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\}\\}");

	private static final Pattern MALFORMED_PLACEHOLDER_PATTERN =
			Pattern.compile("\\{\\{(?!\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\}\\}).*?\\}\\}");

	public List<String> extractVariables(String subject, String body) {
		validateNoMalformedPlaceholders(subject);
		validateNoMalformedPlaceholders(body);

		Set<String> variables = new LinkedHashSet<>();
		variables.addAll(extractFrom(subject));
		variables.addAll(extractFrom(body));
		return List.copyOf(variables);
	}

	public String render(String content, Map<String, String> values) {
		if (content == null || content.isBlank()) {
			return content;
		}

		Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
		StringBuffer rendered = new StringBuffer();
		while (matcher.find()) {
			String name = matcher.group(1);
			if (!values.containsKey(name) || values.get(name) == null) {
				throw new ApiException(
						ErrorCode.VALIDATION_FAILED,
						HttpStatus.BAD_REQUEST,
						"Missing value for template variable: " + name
				);
			}
			matcher.appendReplacement(rendered, Matcher.quoteReplacement(values.get(name)));
		}
		matcher.appendTail(rendered);
		return rendered.toString();
	}

	public String toJsonArray(List<String> variables) {
		return variables.stream()
				.map(v -> "\"" + v + "\"")
				.collect(Collectors.joining(",", "[", "]"));
	}

	public List<String> fromJsonArray(String json) {
		if (json == null || json.isBlank() || "[]".equals(json.trim())) {
			return List.of();
		}
		String trimmed = json.trim();
		if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
			return List.of();
		}
		String inner = trimmed.substring(1, trimmed.length() - 1).trim();
		if (inner.isEmpty()) {
			return List.of();
		}

		List<String> values = new ArrayList<>();
		for (String part : inner.split(",")) {
			String token = part.trim();
			if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
				values.add(token.substring(1, token.length() - 1));
			}
		}
		return List.copyOf(values);
	}

	private List<String> extractFrom(String content) {
		if (content == null || content.isBlank()) {
			return List.of();
		}
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
		Set<String> names = new LinkedHashSet<>();
		while (matcher.find()) {
			names.add(matcher.group(1));
		}
		return List.copyOf(names);
	}

	private void validateNoMalformedPlaceholders(String content) {
		if (content == null || content.isBlank()) {
			return;
		}
		Matcher matcher = MALFORMED_PLACEHOLDER_PATTERN.matcher(content);
		if (matcher.find()) {
			throw new ApiException(
					ErrorCode.VALIDATION_FAILED,
					HttpStatus.BAD_REQUEST,
					"Invalid template placeholder syntax near: " + matcher.group()
			);
		}
	}
}
