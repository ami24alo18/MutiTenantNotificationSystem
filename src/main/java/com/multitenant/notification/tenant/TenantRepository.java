package com.multitenant.notification.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

	boolean existsByCodeIgnoreCase(String code);

	Optional<Tenant> findByCodeIgnoreCase(String code);
}
