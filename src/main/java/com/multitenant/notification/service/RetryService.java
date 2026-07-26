package com.multitenant.notification.service;

import com.multitenant.notification.config.RetryConfig;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import com.multitenant.notification.event.NotificationEvent;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RetryService {

    private final DeliveryRepository deliveryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RetryConfig retryConfig;

    public RetryService(DeliveryRepository deliveryRepository, ApplicationEventPublisher eventPublisher, RetryConfig retryConfig) {
        this.deliveryRepository = deliveryRepository;
        this.eventPublisher = eventPublisher;
        this.retryConfig = retryConfig;
    }

    @Scheduled(fixedRateString = "${notification.retry.initial-delay-ms}")
    public void retryFailedNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Delivery> deliveriesToRetry = deliveryRepository.findByStatusAndNextRetryAtBefore(DeliveryStatus.RETRY, now);

        for (Delivery delivery : deliveriesToRetry) {
            if (delivery.getRetryAttempts() < retryConfig.getMaxAttempts()) {
                log.info("Retrying notification for delivery ID: {}", delivery.getId());
                eventPublisher.publishEvent(new NotificationEvent(this, delivery));
            } else {
                delivery.setStatus(DeliveryStatus.FAILED);
                deliveryRepository.save(delivery);
                log.warn("Max retry attempts reached for delivery ID: {}. Marking as FAILED.", delivery.getId());
            }
        }
    }

    public void scheduleRetry(Delivery delivery) {
        int attempts = delivery.getRetryAttempts() + 1;
        delivery.setRetryAttempts(attempts);

        if (attempts < retryConfig.getMaxAttempts()) {
            long delay = (long) (retryConfig.getInitialDelayMs() * Math.pow(2, attempts - 1));
            delay = Math.min(delay, retryConfig.getMaxDelayMs());

            delivery.setNextRetryAt(LocalDateTime.now().plusNanos(delay * 1_000_000));
            delivery.setStatus(DeliveryStatus.RETRY);
            log.info("Scheduling retry #{} for delivery ID: {} at {}", attempts, delivery.getId(), delivery.getNextRetryAt());
        } else {
            delivery.setStatus(DeliveryStatus.FAILED);
            log.warn("Max retry attempts reached for delivery ID: {}. Marking as FAILED.", delivery.getId());
        }

        deliveryRepository.save(delivery);
    }
}
