package com.multitenant.notification.notification;

import com.multitenant.notification.channel.NotificationChannel;
import com.multitenant.notification.delivery.Delivery;

/**
 * Outbound channel sender (stubbed for local/dev — no real provider I/O).
 */
public interface ChannelSender {

	void send(Delivery delivery) throws ChannelDeliveryException;

	class ChannelDeliveryException extends Exception {
		public ChannelDeliveryException(String message) {
			super(message);
		}

		public ChannelDeliveryException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
