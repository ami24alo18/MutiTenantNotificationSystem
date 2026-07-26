package com.multitenant.notification.common.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

	@Test
	void okWrapsDataWithSuccessFlag() {
		ApiResponse<String> response = ApiResponse.ok("payload");

		assertThat(response.success()).isTrue();
		assertThat(response.data()).isEqualTo("payload");
		assertThat(response.timestamp()).isNotNull();
	}

	@Test
	void okMessageSetsMessageWithoutData() {
		ApiResponse<Void> response = ApiResponse.okMessage("done");

		assertThat(response.success()).isTrue();
		assertThat(response.message()).isEqualTo("done");
		assertThat(response.data()).isNull();
	}
}
