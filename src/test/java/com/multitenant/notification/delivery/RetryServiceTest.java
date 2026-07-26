package com.multitenant.notification.delivery;

import com.multitenant.notification.config.RetryConfig;
import com.multitenant.notification.event.NotificationEvent;
import com.multitenant.notification.service.RetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RetryConfig retryConfig;

    private RetryService retryService;

    @BeforeEach
    void setUp() {
        retryService = new RetryService(deliveryRepository, eventPublisher, retryConfig);
    }

    @Test
    void whenFailedDeliveriesFound_shouldPublishRetryEvents() {
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setRetryAttempts(1);

        when(retryConfig.getMaxAttempts()).thenReturn(3);
        when(deliveryRepository.findByStatusAndNextRetryAtBefore(eq(DeliveryStatus.RETRY), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(delivery));

        retryService.retryFailedNotifications();

        verify(eventPublisher, times(1)).publishEvent(any(NotificationEvent.class));
    }

    @Test
    void whenMaxRetriesReached_shouldMarkAsFailed() {
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setRetryAttempts(3);

        when(retryConfig.getMaxAttempts()).thenReturn(3);
        when(deliveryRepository.findByStatusAndNextRetryAtBefore(eq(DeliveryStatus.RETRY), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(delivery));

        retryService.retryFailedNotifications();

        verify(eventPublisher, never()).publishEvent(any(NotificationEvent.class));
        ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);
        verify(deliveryRepository, times(1)).save(deliveryCaptor.capture());
        assert(deliveryCaptor.getValue().getStatus()).equals(DeliveryStatus.FAILED);
    }
}
