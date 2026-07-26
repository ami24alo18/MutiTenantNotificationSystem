package com.multitenant.notification.notification;

import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryAttemptRepository;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

	@Mock
	private DeliveryRepository deliveryRepository;

	@Mock
	private DeliveryAttemptRepository attemptRepository;

	@Mock
	private ChannelSender channelSender;

	@Mock
	private RetryService retryService;

	@InjectMocks
	private NotificationDispatchService dispatchService;

	@Test
	void skipsAlreadySentDelivery() throws Exception {
		UUID id = UUID.randomUUID();
		Delivery delivery = new Delivery();
		delivery.setId(id);
		delivery.setStatus(DeliveryStatus.SENT);
		when(deliveryRepository.findById(id)).thenReturn(Optional.of(delivery));

		dispatchService.process(id);

		verify(deliveryRepository, never()).claimForProcessing(any(), any());
		verify(channelSender, never()).send(any());
	}

	@Test
	void doesNotSendWhenClaimFails() throws Exception {
		UUID id = UUID.randomUUID();
		Delivery delivery = new Delivery();
		delivery.setId(id);
		delivery.setStatus(DeliveryStatus.PROCESSING);
		when(deliveryRepository.findById(id)).thenReturn(Optional.of(delivery));
		when(deliveryRepository.claimForProcessing(eq(id), any())).thenReturn(0);

		dispatchService.process(id);

		verify(channelSender, never()).send(any());
	}
}
