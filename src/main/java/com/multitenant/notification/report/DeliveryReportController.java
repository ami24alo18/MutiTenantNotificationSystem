package com.multitenant.notification.report;

import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class DeliveryReportController {

    private final DeliveryRepository deliveryRepository;

    @GetMapping("/deliveries")
    public ResponseEntity<List<Delivery>> getDeliveries(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Specification<Delivery> spec = Specification.where(null);

        if (tenantId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledAt"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("scheduledAt"), endDate));
        }

        return ResponseEntity.ok(deliveryRepository.findAll(spec));
    }
}
