package com.multitenant.notification.notification;

import com.multitenant.notification.delivery.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deterministic stub sender used until real providers are integrated.
 * Treats recipients ending with {@code +fail@test.local} as transient failures.
 */
@Component
public class StubChannelSender implements ChannelSender {

	private static final Logger log = LoggerFactory.getLogger(StubChannelSender.class);

	@Override
	public void send(Delivery delivery) throws ChannelDeliveryException {
		if (delivery.getRecipient() != null && delivery.getRecipient().endsWith("+fail@test.local")) {
			throw new ChannelDeliveryException("Simulated transient provider failure");
		}
		log.info(
				"Dispatched {} notification deliveryId={} recipient={}",
				delivery.getChannel(),
				delivery.getId(),
				delivery.getRecipient()
		);
	}
}
