package com.multitenant.notification.common.exception;

import com.multitenant.notification.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(GlobalExceptionHandlerTest.TestValidationController.class)
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void validationFailureReturnsStructuredError() throws Exception {
		mockMvc.perform(post("/api/v1/_test/echo")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.fieldErrors").isArray())
				.andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
	}

	@Test
	void apiExceptionReturnsMappedStatus() throws Exception {
		mockMvc.perform(post("/api/v1/_test/not-found")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"demo\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("Resource not found with id: demo"));
	}

	@RestController
	@RequestMapping("/api/v1/_test")
	static class TestValidationController {

		@PostMapping("/echo")
		public ApiResponse<String> echo(@Valid @RequestBody EchoRequest request) {
			return ApiResponse.ok(request.name());
		}

		@PostMapping("/not-found")
		public ApiResponse<Void> notFound(@Valid @RequestBody EchoRequest request) {
			throw new ResourceNotFoundException("Resource", request.name());
		}
	}

	record EchoRequest(@NotBlank(message = "name is required") String name) {
	}
}
