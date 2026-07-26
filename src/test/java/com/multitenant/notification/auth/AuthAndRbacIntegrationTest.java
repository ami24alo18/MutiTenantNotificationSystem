package com.multitenant.notification.auth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndRbacIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void platformAdminCanLoginCreateTenantAndTenantAdmin() throws Exception {
		String platformToken = login("platform.admin@system.local", "Admin@123");

		MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"acme","name":"Acme Corp"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.code").value("acme"))
				.andReturn();

		String tenantId = objectMapper.readTree(tenantResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(post("/api/v1/users")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email":"admin@acme.test",
								  "password":"Tenant@123",
								  "fullName":"Acme Admin",
								  "role":"TENANT_ADMIN",
								  "tenantId":"%s"
								}
								""".formatted(tenantId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.role").value("TENANT_ADMIN"))
				.andExpect(jsonPath("$.data.tenantId").value(tenantId));

		String tenantToken = login("admin@acme.test", "Tenant@123");

		mockMvc.perform(get("/api/v1/tenants")
						.header("Authorization", "Bearer " + tenantToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(1))
				.andExpect(jsonPath("$.data[0].id").value(tenantId));

		mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"other","name":"Other"}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void loginFailsWithInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"platform.admin@system.local","password":"wrong"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void protectedEndpointsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/users"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void platformAdminCanUpdateTenant() throws Exception {
		String token = login("platform.admin@system.local", "Admin@123");

		MvcResult createResult = mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"globex","name":"Globex"}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		String tenantId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(put("/api/v1/tenants/" + tenantId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Globex Updated","active":true}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Globex Updated"));
	}

	private String login(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andReturn();

		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
		String token = root.path("data").path("accessToken").asText();
		assertThat(token).isNotBlank();
		return token;
	}
}
