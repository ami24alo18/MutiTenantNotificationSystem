package com.multitenant.notification.common.exception;

/**
 * Application-wide error codes returned in API error responses.
 */
public enum ErrorCode {

	VALIDATION_FAILED("VALIDATION_FAILED"),
	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
	BAD_REQUEST("BAD_REQUEST"),
	CONFLICT("CONFLICT"),
	UNAUTHORIZED("UNAUTHORIZED"),
	FORBIDDEN("FORBIDDEN"),
	INTERNAL_ERROR("INTERNAL_ERROR");

	private final String code;

	ErrorCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
