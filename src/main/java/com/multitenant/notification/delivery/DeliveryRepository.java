package com.multitenant.notification.delivery;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {
    List<Delivery> findByStatusAndNextRetryAtBefore(DeliveryStatus status, LocalDateTime now);
}
