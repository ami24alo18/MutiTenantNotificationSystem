package com.multitenant.notification.notification;

import com.multitenant.notification.config.RetryProperties;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Promotes due scheduled/retry deliveries onto the worker queue with simple per-tenant fairness.
 */
@Component
public class NotificationScheduler {

	private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
	private static final int MAX_PER_TENANT_PER_TICK = 2;
	private static final int MAX_BATCH = 50;

	private final DeliveryRepository deliveryRepository;
	private final NotificationQueue notificationQueue;

	public NotificationScheduler(DeliveryRepository deliveryRepository, NotificationQueue notificationQueue) {
		this.deliveryRepository = deliveryRepository;
		this.notificationQueue = notificationQueue;
	}

	@Scheduled(fixedDelayString = "${app.retry.poll-interval-ms:2000}")
	@Transactional
	public void dispatchDueWork() {
		Instant now = Instant.now();
		List<Delivery> due = new ArrayList<>();
		due.addAll(deliveryRepository.findDueScheduled(now));
		due.addAll(deliveryRepository.findDueRetries(now));

		Set<UUID> enqueued = new HashSet<>();
		java.util.Map<UUID, Integer> perTenant = new java.util.HashMap<>();
		int total = 0;

		for (Delivery delivery : due) {
			if (total >= MAX_BATCH) {
				break;
			}
			int count = perTenant.getOrDefault(delivery.getTenantId(), 0);
			if (count >= MAX_PER_TENANT_PER_TICK) {
				continue;
			}
			if (delivery.getStatus() == DeliveryStatus.SCHEDULED) {
				delivery.setStatus(DeliveryStatus.PENDING);
				deliveryRepository.save(delivery);
			}
			if (enqueued.add(delivery.getId())) {
				notificationQueue.enqueue(delivery.getId());
				perTenant.put(delivery.getTenantId(), count + 1);
				total++;
			}
		}

		if (total > 0) {
			log.debug("Enqueued {} due deliveries", total);
		}
	}
}
