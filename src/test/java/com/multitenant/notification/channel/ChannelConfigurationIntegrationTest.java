package com.multitenant.notification.channel;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChannelConfigurationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String tenantToken;
	private String suffix;

	@BeforeEach
	void setUp() throws Exception {
		suffix = UUID.randomUUID().toString().substring(0, 8);
		String platformToken = login("platform.admin@system.local", "Admin@123");

		MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"ch-%s","name":"Channel Tenant"}
								""".formatted(suffix)))
				.andExpect(status().isCreated())
				.andReturn();

		String tenantId = objectMapper.readTree(tenantResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		String email = "ch.admin.%s@test.local".formatted(suffix);
		mockMvc.perform(post("/api/v1/users")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email":"%s",
								  "password":"Tenant@123",
								  "fullName":"Channel Admin",
								  "role":"TENANT_ADMIN",
								  "tenantId":"%s"
								}
								""".formatted(email, tenantId)))
				.andExpect(status().isCreated());

		tenantToken = login(email, "Tenant@123");
	}

	@Test
	void listsDefaultDisabledChannelsAndEnablesEmail() throws Exception {
		mockMvc.perform(get("/api/v1/channels")
						.header("Authorization", "Bearer " + tenantToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(4))
				.andExpect(jsonPath("$.data[0].enabled").value(false));

		mockMvc.perform(put("/api/v1/channels/EMAIL")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "enabled":true,
								  "provider":"smtp",
								  "settings":{"fromAddress":"noreply@acme.test","fromName":"Acme"}
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.channel").value("EMAIL"))
				.andExpect(jsonPath("$.data.enabled").value(true))
				.andExpect(jsonPath("$.data.settings.fromAddress").value("noreply@acme.test"));

		mockMvc.perform(put("/api/v1/channels/EMAIL/enabled")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"enabled":false}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.enabled").value(false));
	}

	@Test
	void cannotEnableSmsWithoutSenderId() throws Exception {
		mockMvc.perform(put("/api/v1/channels/SMS")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"enabled":true,"settings":{}}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
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
		String token = root.path("data").path("accessToken").asText();
		assertThat(token).isNotBlank();
		return token;
	}
}
