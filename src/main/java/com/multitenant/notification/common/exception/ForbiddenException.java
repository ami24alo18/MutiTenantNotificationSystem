package com.multitenant.notification.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the caller is authenticated but not allowed to perform the action.
 */
public class ForbiddenException extends ApiException {

	public ForbiddenException(String message) {
		super(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, message);
	}
}
