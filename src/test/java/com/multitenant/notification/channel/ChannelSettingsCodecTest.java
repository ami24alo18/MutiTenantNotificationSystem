package com.multitenant.notification.channel;

import com.multitenant.notification.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChannelSettingsCodecTest {

	private final ChannelSettingsCodec codec = new ChannelSettingsCodec();

	@Test
	void encodeDecodeRoundTrip() {
		Map<String, String> settings = Map.of("fromAddress", "noreply@acme.test", "fromName", "Acme");
		String json = codec.encode(settings);
		assertThat(codec.decode(json))
				.containsEntry("fromAddress", "noreply@acme.test")
				.containsEntry("fromName", "Acme");
	}

	@Test
	void enableEmailRequiresFromAddress() {
		assertThatThrownBy(() -> codec.validateForEnable(NotificationChannel.EMAIL, true, Map.of()))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("fromAddress");
	}

	@Test
	void disableSkipsRequiredSettings() {
		codec.validateForEnable(NotificationChannel.SMS, false, Map.of());
	}
}
