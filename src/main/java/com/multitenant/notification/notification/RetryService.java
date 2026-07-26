package com.multitenant.notification.notification;

import com.multitenant.notification.config.RetryProperties;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RetryService {

	private static final Logger log = LoggerFactory.getLogger(RetryService.class);

	private final DeliveryRepository deliveryRepository;
	private final RetryProperties retryProperties;

	public RetryService(DeliveryRepository deliveryRepository, RetryProperties retryProperties) {
		this.deliveryRepository = deliveryRepository;
		this.retryProperties = retryProperties;
	}

	@Transactional
	public void scheduleRetry(Delivery delivery, String errorMessage) {
		int attempts = delivery.getRetryAttempts() + 1;
		delivery.setRetryAttempts(attempts);
		delivery.setLastError(truncate(errorMessage));

		if (attempts >= retryProperties.getMaxAttempts()) {
			delivery.setStatus(DeliveryStatus.FAILED);
			delivery.setNextRetryAt(null);
			log.warn("Max retries reached for delivery {}", delivery.getId());
		}
		else {
			long delay = (long) (retryProperties.getInitialDelayMs() * Math.pow(2, attempts - 1));
			delay = Math.min(delay, retryProperties.getMaxDelayMs());
			delivery.setStatus(DeliveryStatus.RETRY);
			delivery.setNextRetryAt(Instant.now().plusMillis(delay));
			log.info("Scheduled retry #{} for delivery {} at {}", attempts, delivery.getId(), delivery.getNextRetryAt());
		}
		deliveryRepository.save(delivery);
	}

	private static String truncate(String message) {
		if (message == null) {
			return null;
		}
		return message.length() <= 1000 ? message : message.substring(0, 1000);
	}
}
