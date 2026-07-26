package com.multitenant.notificatin.system.listener;

import com.multitenant.notificatin.system.event.NotificationEvent;
import com.multitenant.notificatin.system.model.Delivery;
import com.multitenant.notificatin.system.model.DeliveryStatus;
import com.multitenant.notificatin.system.repository.DeliveryRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    private final DeliveryRepository deliveryRepository;

    public NotificationEventListener(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        Delivery delivery = event.getDelivery();
        log.info("Processing notification for delivery ID: {}", delivery.getId());

        // Simulate sending the notification
        try {
            Thread.sleep(2000); // Simulate network latency
            delivery.setStatus(DeliveryStatus.SENT);
            delivery.setSentAt(LocalDateTime.now());
            log.info("Notification sent successfully for delivery ID: {}", delivery.getId());
        } catch (InterruptedException e) {
            delivery.setStatus(DeliveryStatus.FAILED);
            log.error("Notification failed for delivery ID: {}. Error: {}", delivery.getId(), e.getMessage());
            Thread.currentThread().interrupt();
        }

        deliveryRepository.save(delivery);
    }
}
