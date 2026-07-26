package com.multitenant.notification.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Bounded in-process queue backed by {@code notificationTaskExecutor}.
 */
@Component
public class ExecutorNotificationQueue implements NotificationQueue {

	private final Executor notificationTaskExecutor;
	private final NotificationDispatchService dispatchService;

	public ExecutorNotificationQueue(
			@Qualifier("notificationTaskExecutor") Executor notificationTaskExecutor,
			NotificationDispatchService dispatchService
	) {
		this.notificationTaskExecutor = notificationTaskExecutor;
		this.dispatchService = dispatchService;
	}

	@Override
	public void enqueue(UUID deliveryId) {
		notificationTaskExecutor.execute(() -> dispatchService.process(deliveryId));
	}
}
