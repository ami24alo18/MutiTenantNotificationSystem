package com.multitenant.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Enables Bean Validation for {@code @RequestBody} (via starter-validation)
 * and method-parameter validation across the application.
 */
@Configuration
public class ValidationConfig {

	/**
	 * Enables constraint annotations on service and controller method parameters
	 * (in addition to request-body validation).
	 */
	@Bean
	public static MethodValidationPostProcessor methodValidationPostProcessor() {
		return new MethodValidationPostProcessor();
	}
}
