package com.multitenant.notification.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base unchecked exception for domain/API errors with an HTTP status and error code.
 */
public class ApiException extends RuntimeException {

	private final ErrorCode errorCode;
	private final HttpStatus status;

	public ApiException(ErrorCode errorCode, HttpStatus status, String message) {
		super(message);
		this.errorCode = errorCode;
		this.status = status;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
