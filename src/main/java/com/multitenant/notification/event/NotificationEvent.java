package com.multitenant.notification.event;

import com.multitenant.notification.delivery.Delivery;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final Delivery delivery;

    public NotificationEvent(Object source, Delivery delivery) {
        super(source);
        this.delivery = delivery;
    }
}
