package com.multitenant.notification.notification;

import com.multitenant.notification.config.RetryProperties;
import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryServiceTest {

	@Mock
	private DeliveryRepository deliveryRepository;

	@Mock
	private RetryProperties retryProperties;

	@InjectMocks
	private RetryService retryService;

	@Test
	void schedulesExponentialBackoffRetry() {
		when(retryProperties.getMaxAttempts()).thenReturn(5);
		when(retryProperties.getInitialDelayMs()).thenReturn(1000L);
		when(retryProperties.getMaxDelayMs()).thenReturn(60000L);

		Delivery delivery = new Delivery();
		delivery.setId(UUID.randomUUID());
		delivery.setRetryAttempts(0);

		retryService.scheduleRetry(delivery, "boom");

		ArgumentCaptor<Delivery> captor = ArgumentCaptor.forClass(Delivery.class);
		verify(deliveryRepository).save(captor.capture());
		Delivery saved = captor.getValue();
		assertThat(saved.getStatus()).isEqualTo(DeliveryStatus.RETRY);
		assertThat(saved.getRetryAttempts()).isEqualTo(1);
		assertThat(saved.getNextRetryAt()).isNotNull();
		assertThat(saved.getLastError()).isEqualTo("boom");
	}

	@Test
	void marksFailedWhenMaxAttemptsReached() {
		when(retryProperties.getMaxAttempts()).thenReturn(2);

		Delivery delivery = new Delivery();
		delivery.setId(UUID.randomUUID());
		delivery.setRetryAttempts(1);

		retryService.scheduleRetry(delivery, "still failing");

		ArgumentCaptor<Delivery> captor = ArgumentCaptor.forClass(Delivery.class);
		verify(deliveryRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(DeliveryStatus.FAILED);
		assertThat(captor.getValue().getRetryAttempts()).isEqualTo(2);
	}
}
