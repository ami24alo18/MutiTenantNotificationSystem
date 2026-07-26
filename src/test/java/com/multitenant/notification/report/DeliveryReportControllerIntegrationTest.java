package com.multitenant.notification.report;

import com.multitenant.notification.delivery.Delivery;
import com.multitenant.notification.delivery.DeliveryRepository;
import com.multitenant.notification.delivery.DeliveryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeliveryReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @BeforeEach
    void setUp() {
        deliveryRepository.deleteAll();

        Delivery d1 = new Delivery();
        d1.setTenantId(1L);
        d1.setStatus(DeliveryStatus.SENT);
        d1.setScheduledAt(LocalDateTime.now().minusDays(1));
        deliveryRepository.save(d1);

        Delivery d2 = new Delivery();
        d2.setTenantId(2L);
        d2.setStatus(DeliveryStatus.FAILED);
        d2.setScheduledAt(LocalDateTime.now());
        deliveryRepository.save(d2);
    }

    @Test
    void whenFilterByTenant_shouldReturnCorrectDeliveries() throws Exception {
        mockMvc.perform(get("/reports/deliveries?tenantId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenantId").value(1));
    }

    @Test
    void whenFilterByStatus_shouldReturnCorrectDeliveries() throws Exception {
        mockMvc.perform(get("/reports/deliveries?status=FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("FAILED"));
    }
}
