package com.multitenant.notification.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error envelope for API failures.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
		boolean success,
		String code,
		String message,
		List<FieldErrorDetail> fieldErrors,
		Instant timestamp,
		String path
) {

	public ErrorResponse(String code, String message, String path) {
		this(false, code, message, null, Instant.now(), path);
	}

	public ErrorResponse(String code, String message, List<FieldErrorDetail> fieldErrors, String path) {
		this(false, code, message, fieldErrors, Instant.now(), path);
	}

	public record FieldErrorDetail(String field, String message, Object rejectedValue) {
	}
}
