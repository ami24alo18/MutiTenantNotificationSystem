package com.multitenant.notification.channel.dto;

import com.multitenant.notification.channel.ChannelConfiguration;
import com.multitenant.notification.channel.ChannelSettingsCodec;
import com.multitenant.notification.channel.NotificationChannel;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ChannelConfigResponse(
		UUID id,
		UUID tenantId,
		NotificationChannel channel,
		boolean enabled,
		String provider,
		Map<String, String> settings,
		Instant createdAt,
		Instant updatedAt
) {

	public static ChannelConfigResponse from(ChannelConfiguration config, ChannelSettingsCodec codec) {
		return new ChannelConfigResponse(
				config.getId(),
				config.getTenant().getId(),
				config.getChannel(),
				config.isEnabled(),
				config.getProvider(),
				codec.decode(config.getSettingsJson()),
				config.getCreatedAt(),
				config.getUpdatedAt()
		);
	}
}
