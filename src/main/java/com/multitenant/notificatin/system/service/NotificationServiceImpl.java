package com.multitenant.notificatin.system.service;

import com.multitenant.notificatin.system.dto.NotificationRequest;
import com.multitenant.notificatin.system.model.Delivery;
import com.multitenant.notificatin.system.model.DeliveryStatus;
import com.multitenant.notificatin.system.repository.DeliveryRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final DeliveryRepository deliveryRepository;

    public NotificationServiceImpl(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        saveDelivery(request, DeliveryStatus.PENDING, null);
    }

    @Override
    public void scheduleNotification(NotificationRequest request) {
        saveDelivery(request, DeliveryStatus.SCHEDULED, request.getScheduledAt());
    }

    private void saveDelivery(NotificationRequest request, DeliveryStatus status, LocalDateTime scheduledAt) {
        Delivery delivery = new Delivery();
        // Hardcoding tenantId for now, will be replaced with authenticated user's tenant
        delivery.setTenantId(1L);
        delivery.setChannel(request.getChannel());
        delivery.setRecipient(request.getRecipient());
        delivery.setContent(request.getContent());
        delivery.setStatus(status);
        delivery.setScheduledAt(scheduledAt);
        deliveryRepository.save(delivery);
    }
}
