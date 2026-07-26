package com.multitenant.notification.listener;

import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import com.multitenant.notification.event.NotificationEvent;
import com.multitenant.notification.service.RetryService;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    private final DeliveryRepository deliveryRepository;
    private final RetryService retryService;

    public NotificationEventListener(DeliveryRepository deliveryRepository, RetryService retryService) {
        this.deliveryRepository = deliveryRepository;
        this.retryService = retryService;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        Delivery delivery = event.getDelivery();
        log.info("Processing notification for delivery ID: {}", delivery.getId());

        try {
            // Simulate a transient failure for demonstration
            if (delivery.getRetryAttempts() < 2) {
                throw new RuntimeException("Simulated network failure");
            }

            Thread.sleep(1000); // Simulate network latency
            delivery.setStatus(DeliveryStatus.SENT);
            delivery.setSentAt(LocalDateTime.now());
            deliveryRepository.save(delivery);
            log.info("Notification sent successfully for delivery ID: {}", delivery.getId());
        } catch (Exception e) {
            log.error("Notification failed for delivery ID: {}. Error: {}", delivery.getId(), e.getMessage());
            retryService.scheduleRetry(delivery);
        }
    }
}
