package com.multitenant.notification.delivery;

import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByStatusAndNextRetryAtBefore(DeliveryStatus status, LocalDateTime now);
}
