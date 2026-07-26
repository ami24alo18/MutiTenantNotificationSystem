package com.multitenant.notificatin.system.service;

import com.multitenant.notificatin.system.dto.NotificationRequest;
import com.multitenant.notificatin.system.event.NotificationEvent;
import com.multitenant.notificatin.system.model.Delivery;
import com.multitenant.notificatin.system.model.DeliveryStatus;
import com.multitenant.notificatin.system.repository.DeliveryRepository;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final DeliveryRepository deliveryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationServiceImpl(DeliveryRepository deliveryRepository, ApplicationEventPublisher eventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        Delivery delivery = saveDelivery(request, DeliveryStatus.PENDING, null);
        eventPublisher.publishEvent(new NotificationEvent(this, delivery));
    }

    @Override
    public void scheduleNotification(NotificationRequest request) {
        saveDelivery(request, DeliveryStatus.SCHEDULED, request.getScheduledAt());
    }

    private Delivery saveDelivery(NotificationRequest request, DeliveryStatus status, LocalDateTime scheduledAt) {
        Delivery delivery = new Delivery();
        // Hardcoding tenantId for now, will be replaced with authenticated user's tenant
        delivery.setTenantId(1L);
        delivery.setChannel(request.getChannel());
        delivery.setRecipient(request.getRecipient());
        delivery.setContent(request.getContent());
        delivery.setStatus(status);
        delivery.setScheduledAt(scheduledAt);
        return deliveryRepository.save(delivery);
    }
}
