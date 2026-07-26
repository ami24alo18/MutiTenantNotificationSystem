package com.multitenant.notification.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multitenant.notification.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    void whenValidRequest_sendNotificationShouldSucceed() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setRecipient("test@example.com");
        request.setContent("Test content");
        request.setChannel("email");

        mockMvc.perform(post("/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        // Verify delivery is created with PENDING status
        Delivery delivery = deliveryRepository.findAll().get(0);
        assert(delivery.getStatus()).equals(DeliveryStatus.PENDING);
    }

    @Test
    void whenInvalidRequest_sendNotificationShouldFail() throws Exception {
        NotificationRequest request = new NotificationRequest(); // Missing required fields
        mockMvc.perform(post("/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
