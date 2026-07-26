package com.multitenant.notification.notification.dto;

import com.multitenant.notification.delivery.DeliveryAttempt;

import java.time.Instant;
import java.util.UUID;

public record DeliveryAttemptResponse(
		UUID id,
		UUID deliveryId,
		int attemptNumber,
		boolean success,
		String responseMessage,
		Instant attemptedAt
) {

	public static DeliveryAttemptResponse from(DeliveryAttempt attempt) {
		return new DeliveryAttemptResponse(
				attempt.getId(),
				attempt.getDelivery().getId(),
				attempt.getAttemptNumber(),
				attempt.isSuccess(),
				attempt.getResponseMessage(),
				attempt.getAttemptedAt()
		);
	}
}
