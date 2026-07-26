package com.multitenant.notification.notification;

import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryAttempt;
import com.multitenant.notification.delivery.DeliveryAttemptRepository;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationDispatchService {

	private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

	private final DeliveryRepository deliveryRepository;
	private final DeliveryAttemptRepository attemptRepository;
	private final ChannelSender channelSender;
	private final RetryService retryService;

	public NotificationDispatchService(
			DeliveryRepository deliveryRepository,
			DeliveryAttemptRepository attemptRepository,
			ChannelSender channelSender,
			RetryService retryService
	) {
		this.deliveryRepository = deliveryRepository;
		this.attemptRepository = attemptRepository;
		this.channelSender = channelSender;
		this.retryService = retryService;
	}

	@Transactional
	public void process(UUID deliveryId) {
		Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
		if (delivery == null) {
			return;
		}

		// Idempotency: never re-send a successful delivery.
		if (delivery.getStatus() == DeliveryStatus.SENT) {
			log.info("Skipping already-sent delivery {}", deliveryId);
			return;
		}

		int claimed = deliveryRepository.claimForProcessing(deliveryId, Instant.now());
		if (claimed == 0) {
			log.debug("Could not claim delivery {} (already processing/sent)", deliveryId);
			return;
		}

		delivery = deliveryRepository.findById(deliveryId).orElseThrow();
		int attemptNumber = delivery.getRetryAttempts() + 1;

		try {
			channelSender.send(delivery);
			delivery.setStatus(DeliveryStatus.SENT);
			delivery.setSentAt(Instant.now());
			delivery.setLastError(null);
			delivery.setNextRetryAt(null);
			deliveryRepository.save(delivery);
			persistAttempt(delivery, attemptNumber, true, "OK");
		}
		catch (ChannelSender.ChannelDeliveryException ex) {
			persistAttempt(delivery, attemptNumber, false, ex.getMessage());
			retryService.scheduleRetry(delivery, ex.getMessage());
		}
		catch (Exception ex) {
			log.error("Unexpected dispatch error for {}", deliveryId, ex);
			persistAttempt(delivery, attemptNumber, false, ex.getMessage());
			retryService.scheduleRetry(delivery, ex.getMessage());
		}
	}

	private void persistAttempt(Delivery delivery, int attemptNumber, boolean success, String message) {
		DeliveryAttempt attempt = new DeliveryAttempt();
		attempt.setDelivery(delivery);
		attempt.setAttemptNumber(attemptNumber);
		attempt.setSuccess(success);
		attempt.setResponseMessage(message);
		attemptRepository.save(attempt);
	}
}
