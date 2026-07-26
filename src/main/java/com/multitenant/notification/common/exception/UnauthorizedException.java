package com.multitenant.notification.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails (invalid credentials, inactive user, etc.).
 */
public class UnauthorizedException extends ApiException {

	public UnauthorizedException(String message) {
		super(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, message);
	}
}
