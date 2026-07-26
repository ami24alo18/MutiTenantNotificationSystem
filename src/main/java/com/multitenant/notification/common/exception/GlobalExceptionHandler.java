package com.multitenant.notification.common.exception;

import com.multitenant.notification.common.response.ErrorResponse;
import com.multitenant.notification.common.response.ErrorResponse.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Centralized exception-to-HTTP mapping for consistent API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
		log.warn("API error [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage());
		ErrorResponse body = new ErrorResponse(
				ex.getErrorCode().getCode(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(ex.getStatus()).body(body);
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
			AuthorizationDeniedException ex,
			HttpServletRequest request
	) {
		ErrorResponse body = new ErrorResponse(
				ErrorCode.FORBIDDEN.getCode(),
				"Access denied",
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorDetail(
						error.getField(),
						error.getDefaultMessage(),
						error.getRejectedValue()
				))
				.toList();

		ErrorResponse body = new ErrorResponse(
				ErrorCode.VALIDATION_FAILED.getCode(),
				"Request validation failed",
				fieldErrors,
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
		List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorDetail(
						error.getField(),
						error.getDefaultMessage(),
						error.getRejectedValue()
				))
				.toList();

		ErrorResponse body = new ErrorResponse(
				ErrorCode.VALIDATION_FAILED.getCode(),
				"Request validation failed",
				fieldErrors,
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request
	) {
		List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
				.map(violation -> new FieldErrorDetail(
						violation.getPropertyPath().toString(),
						violation.getMessage(),
						violation.getInvalidValue()
				))
				.toList();

		ErrorResponse body = new ErrorResponse(
				ErrorCode.VALIDATION_FAILED.getCode(),
				"Request validation failed",
				fieldErrors,
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
		ErrorResponse body = new ErrorResponse(
				ErrorCode.BAD_REQUEST.getCode(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(
			HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request
	) {
		ErrorResponse body = new ErrorResponse(
				ErrorCode.BAD_REQUEST.getCode(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(
			NoResourceFoundException ex,
			HttpServletRequest request
	) {
		ErrorResponse body = new ErrorResponse(
				ErrorCode.RESOURCE_NOT_FOUND.getCode(),
				"Endpoint not found",
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
		log.error("Unhandled error at {}", request.getRequestURI(), ex);
		ErrorResponse body = new ErrorResponse(
				ErrorCode.INTERNAL_ERROR.getCode(),
				"An unexpected error occurred",
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
