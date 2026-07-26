package com.multitenant.notification.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource cannot be found.
 */
public class ResourceNotFoundException extends ApiException {

	public ResourceNotFoundException(String message) {
		super(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, message);
	}

	public ResourceNotFoundException(String resourceType, Object id) {
		this("%s not found with id: %s".formatted(resourceType, id));
	}
}
