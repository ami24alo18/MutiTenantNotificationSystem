package com.multitenant.notification.notification;

import java.util.UUID;

/**
 * Abstraction over the outbound notification work queue.
 */
public interface NotificationQueue {

	void enqueue(UUID deliveryId);
}
