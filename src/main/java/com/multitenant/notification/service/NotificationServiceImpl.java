package com.multitenant.notification.service;

import com.multitenant.notification.dto.NotificationRequest;
import com.multitenant.notification.event.NotificationEvent;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryStatus;
import com.multitenant.notification.delivery.DeliveryRepository;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final DeliveryRepository deliveryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RateLimitingService rateLimitingService;

    public NotificationServiceImpl(DeliveryRepository deliveryRepository, ApplicationEventPublisher eventPublisher, RateLimitingService rateLimitingService) {
        this.deliveryRepository = deliveryRepository;
        this.eventPublisher = eventPublisher;
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        // Hardcoding tenantId for now, will be replaced with authenticated user's tenant
        Long tenantId = 1L;
        if (!rateLimitingService.tryAcquire(tenantId)) {
            throw new ServiceUnavailable("Rate limit exceeded");
        }
        Delivery delivery = saveDelivery(request, DeliveryStatus.PENDING, null, tenantId);
        eventPublisher.publishEvent(new NotificationEvent(this, delivery));
    }

    @Override
    public void scheduleNotification(NotificationRequest request) {
        // Hardcoding tenantId for now, will be replaced with authenticated user's tenant
        Long tenantId = 1L;
        if (!rateLimitingService.tryAcquire(tenantId)) {
            throw new ServiceUnavailable("Rate limit exceeded");
        }
        saveDelivery(request, DeliveryStatus.SCHEDULED, request.getScheduledAt(), tenantId);
    }

    private Delivery saveDelivery(NotificationRequest request, DeliveryStatus status, LocalDateTime scheduledAt, Long tenantId) {
        Delivery delivery = new Delivery();
        delivery.setTenantId(tenantId);
        delivery.setChannel(request.getChannel());
        delivery.setRecipient(request.getRecipient());
        delivery.setContent(request.getContent());
        delivery.setStatus(status);
        delivery.setScheduledAt(scheduledAt);
        return deliveryRepository.save(delivery);
    }
}
