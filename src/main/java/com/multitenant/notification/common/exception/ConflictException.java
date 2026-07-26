package com.multitenant.notification.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a create/update conflicts with an existing resource.
 */
public class ConflictException extends ApiException {

	public ConflictException(String message) {
		super(ErrorCode.CONFLICT, HttpStatus.CONFLICT, message);
	}
}
