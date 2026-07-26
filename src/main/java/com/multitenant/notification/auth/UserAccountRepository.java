package com.multitenant.notification.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

	Optional<UserAccount> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByRole(Role role);

	List<UserAccount> findByTenantId(UUID tenantId);

	List<UserAccount> findByTenantIsNull();
}
