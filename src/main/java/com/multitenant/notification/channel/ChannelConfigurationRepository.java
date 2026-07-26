package com.multitenant.notification.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelConfigurationRepository extends JpaRepository<ChannelConfiguration, UUID> {

	List<ChannelConfiguration> findByTenantIdOrderByChannelAsc(UUID tenantId);

	Optional<ChannelConfiguration> findByTenantIdAndChannel(UUID tenantId, NotificationChannel channel);

	boolean existsByTenantIdAndChannel(UUID tenantId, NotificationChannel channel);
}
