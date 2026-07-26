package com.multitenant.notification.template;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationTemplateIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String platformToken;
	private String tenantToken;
	private String tenantId;
	private String suffix;

	@BeforeEach
	void setUp() throws Exception {
		suffix = UUID.randomUUID().toString().substring(0, 8);
		platformToken = login("platform.admin@system.local", "Admin@123");

		MvcResult tenantResult = mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"tmpl-%s","name":"Template Tenant"}
								""".formatted(suffix)))
				.andExpect(status().isCreated())
				.andReturn();

		tenantId = objectMapper.readTree(tenantResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		String adminEmail = "tmpl.admin.%s@test.local".formatted(suffix);
		mockMvc.perform(post("/api/v1/users")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email":"%s",
								  "password":"Tenant@123",
								  "fullName":"Template Admin",
								  "role":"TENANT_ADMIN",
								  "tenantId":"%s"
								}
								""".formatted(adminEmail, tenantId)))
				.andExpect(status().isCreated());

		tenantToken = login(adminEmail, "Tenant@123");
	}

	@Test
	void tenantAdminCanCreateListPreviewAndUpdateTemplate() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/templates")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code":"welcome-email",
								  "name":"Welcome Email",
								  "channel":"EMAIL",
								  "subject":"Welcome {{firstName}}",
								  "body":"Hello {{firstName}}, your id is {{userId}}."
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.code").value("welcome-email"))
				.andExpect(jsonPath("$.data.variables[0]").value("firstName"))
				.andExpect(jsonPath("$.data.variables[1]").value("userId"))
				.andReturn();

		String templateId = objectMapper.readTree(createResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(get("/api/v1/templates")
						.header("Authorization", "Bearer " + tenantToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(1));

		mockMvc.perform(post("/api/v1/templates/" + templateId + "/preview")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"variables":{"firstName":"Ada","userId":"42"}}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.subject").value("Welcome Ada"))
				.andExpect(jsonPath("$.data.body").value("Hello Ada, your id is 42."));

		mockMvc.perform(put("/api/v1/templates/" + templateId)
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":"Welcome Email v2",
								  "body":"Hi {{firstName}}"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Welcome Email v2"))
				.andExpect(jsonPath("$.data.variables.length()").value(1))
				.andExpect(jsonPath("$.data.variables[0]").value("firstName"));

		mockMvc.perform(delete("/api/v1/templates/" + templateId)
						.header("Authorization", "Bearer " + tenantToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Template deactivated"));
	}

	@Test
	void emailTemplateRequiresSubject() throws Exception {
		mockMvc.perform(post("/api/v1/templates")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code":"no-subject",
								  "name":"Broken",
								  "channel":"EMAIL",
								  "body":"Hello"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
	}

	@Test
	void tenantCannotAccessOtherTenantTemplate() throws Exception {
		MvcResult otherTenant = mockMvc.perform(post("/api/v1/tenants")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"other-%s","name":"Other Tenant"}
								""".formatted(suffix)))
				.andExpect(status().isCreated())
				.andReturn();

		String otherTenantId = objectMapper.readTree(otherTenant.getResponse().getContentAsString())
				.path("data").path("id").asText();

		MvcResult templateResult = mockMvc.perform(post("/api/v1/templates")
						.header("Authorization", "Bearer " + platformToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code":"other-sms",
								  "name":"Other SMS",
								  "channel":"SMS",
								  "body":"Ping {{name}}",
								  "tenantId":"%s"
								}
								""".formatted(otherTenantId)))
				.andExpect(status().isCreated())
				.andReturn();

		String otherTemplateId = objectMapper.readTree(templateResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(get("/api/v1/templates/" + otherTemplateId)
						.header("Authorization", "Bearer " + tenantToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejectsMalformedPlaceholderSyntax() throws Exception {
		mockMvc.perform(post("/api/v1/templates")
						.header("Authorization", "Bearer " + tenantToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "code":"bad-placeholder",
								  "name":"Bad",
								  "channel":"SMS",
								  "body":"Hello {{}}"
								}
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
