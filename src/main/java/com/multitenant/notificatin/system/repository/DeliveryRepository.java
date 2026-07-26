package com.multitenant.notificatin.system.repository;

import com.multitenant.notificatin.system.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
