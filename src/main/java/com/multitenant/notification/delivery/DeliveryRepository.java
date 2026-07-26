package com.multitenant.notification.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery> {

	Optional<Delivery> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);

	Optional<Delivery> findByIdAndTenantId(UUID id, UUID tenantId);

	List<Delivery> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

	@Query("""
			SELECT d FROM Delivery d
			WHERE d.status = com.multitenant.notification.delivery.DeliveryStatus.SCHEDULED
			  AND d.scheduledAt <= :now
			ORDER BY d.scheduledAt ASC
			""")
	List<Delivery> findDueScheduled(@Param("now") Instant now);

	@Query("""
			SELECT d FROM Delivery d
			WHERE d.status = com.multitenant.notification.delivery.DeliveryStatus.RETRY
			  AND d.nextRetryAt <= :now
			ORDER BY d.nextRetryAt ASC
			""")
	List<Delivery> findDueRetries(@Param("now") Instant now);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			UPDATE Delivery d
			SET d.status = com.multitenant.notification.delivery.DeliveryStatus.PROCESSING,
			    d.processingAt = :now,
			    d.version = d.version + 1
			WHERE d.id = :id
			  AND d.status IN (
			      com.multitenant.notification.delivery.DeliveryStatus.PENDING,
			      com.multitenant.notification.delivery.DeliveryStatus.RETRY
			  )
			  AND d.status <> com.multitenant.notification.delivery.DeliveryStatus.SENT
			""")
	int claimForProcessing(@Param("id") UUID id, @Param("now") Instant now);
}
