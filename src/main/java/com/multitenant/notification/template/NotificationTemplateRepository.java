package com.multitenant.notification.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

	boolean existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

	Optional<NotificationTemplate> findByIdAndTenantId(UUID id, UUID tenantId);

	List<NotificationTemplate> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

	List<NotificationTemplate> findAllByOrderByCreatedAtDesc();
}
