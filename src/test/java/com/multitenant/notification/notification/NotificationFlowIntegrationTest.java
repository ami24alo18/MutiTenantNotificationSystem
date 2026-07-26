package com.multitenant.notification.notification;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String token;
	private String templateId;
	private String suffix;

	@BeforeEach
	void setUp() throws Exception {
		suffix = UUID.randomUUID().toString().substring(0, 8);
		String platformToken = login("platform.admin@system.local", "Admin@123");

		MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"n-%s","name":"Notify Tenant"}
								""".formatted(suffix)))
				.andExpect(status().isCreated())
				.andReturn();
		String tenantId = objectMapper.readTree(tenantResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		String email = "n.admin.%s@test.local".formatted(suffix);
		mockMvc.perform(post("/api/v1/users")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email":"%s","password":"Tenant@123","fullName":"N Admin",
								  "role":"TENANT_ADMIN","tenantId":"%s"
								}
								""".formatted(email, tenantId)))
				.andExpect(status().isCreated());
		token = login(email, "Tenant@123");

		mockMvc.perform(put("/api/v1/channels/EMAIL")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"enabled":true,"provider":"smtp","settings":{"fromAddress":"noreply@test.local"}}
								"""))
				.andExpect(status().isOk());

		MvcResult templateResult = mockMvc.perform(post("/api/v1/templates")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code":"welcome","name":"Welcome","channel":"EMAIL",
								  "subject":"Hi {{name}}","body":"Hello {{name}}"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		templateId = objectMapper.readTree(templateResult.getResponse().getContentAsString())
				.path("data").path("id").asText();
	}

	@Test
	void sendNotificationProcessesAsynchronouslyAndIsIdempotent() throws Exception {
		String key = "idem-" + suffix;
		MvcResult first = mockMvc.perform(post("/api/v1/notifications/send")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "templateId":"%s","recipient":"user@test.local","channel":"EMAIL",
								  "variables":{"name":"Ada"},"idempotencyKey":"%s"
								}
								""".formatted(templateId, key)))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.data.content").value("Hello Ada"))
				.andReturn();

		String deliveryId = objectMapper.readTree(first.getResponse().getContentAsString())
				.path("data").path("id").asText();

		String status = waitForStatus(deliveryId, "SENT", 5000);
		assertThat(status).isEqualTo("SENT");

		mockMvc.perform(post("/api/v1/notifications/send")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "templateId":"%s","recipient":"user@test.local","channel":"EMAIL",
								  "variables":{"name":"Ada"},"idempotencyKey":"%s"
								}
								""".formatted(templateId, key)))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.data.id").value(deliveryId));

		mockMvc.perform(get("/api/v1/reports/deliveries/" + deliveryId + "/attempts")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].success").value(true));
	}

	private String waitForStatus(String deliveryId, String expected, long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		String latest = null;
		while (System.currentTimeMillis() < deadline) {
			MvcResult result = mockMvc.perform(get("/api/v1/notifications/" + deliveryId)
							.header("Authorization", "Bearer " + token))
					.andExpect(status().isOk())
					.andReturn();
			latest = objectMapper.readTree(result.getResponse().getContentAsString())
					.path("data").path("status").asText();
			if (expected.equals(latest)) {
				return latest;
			}
			Thread.sleep(100);
		}
		return latest;
	}

	private String login(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
		String accessToken = root.path("data").path("accessToken").asText();
		assertThat(accessToken).isNotBlank();
		return accessToken;
	}
}
