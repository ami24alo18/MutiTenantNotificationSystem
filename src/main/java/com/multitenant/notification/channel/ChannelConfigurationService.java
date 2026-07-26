package com.multitenant.notification.channel;

import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.channel.dto.ChannelConfigResponse;
import com.multitenant.notification.channel.dto.ToggleChannelRequest;
import com.multitenant.notification.channel.dto.UpsertChannelConfigRequest;
import com.multitenant.notification.common.exception.ApiException;
import com.multitenant.notification.common.exception.ErrorCode;
import com.multitenant.notification.common.exception.ForbiddenException;
import com.multitenant.notification.common.exception.ResourceNotFoundException;
import com.multitenant.notification.tenant.Tenant;
import com.multitenant.notification.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChannelConfigurationService {

	private final ChannelConfigurationRepository channelRepository;
	private final TenantRepository tenantRepository;
	private final ChannelSettingsCodec settingsCodec;

	public ChannelConfigurationService(
			ChannelConfigurationRepository channelRepository,
			TenantRepository tenantRepository,
			ChannelSettingsCodec settingsCodec
	) {
		this.channelRepository = channelRepository;
		this.tenantRepository = tenantRepository;
		this.settingsCodec = settingsCodec;
	}

	@Transactional
	public List<ChannelConfigResponse> list(UUID requestedTenantId, UserPrincipal actor) {
		UUID tenantId = resolveTenantId(requestedTenantId, actor);
		ensureDefaults(tenantId);
		return channelRepository.findByTenantIdOrderByChannelAsc(tenantId).stream()
				.map(config -> ChannelConfigResponse.from(config, settingsCodec))
				.toList();
	}

	@Transactional
	public ChannelConfigResponse get(NotificationChannel channel, UUID requestedTenantId, UserPrincipal actor) {
		UUID tenantId = resolveTenantId(requestedTenantId, actor);
		ensureDefaults(tenantId);
		ChannelConfiguration config = channelRepository.findByTenantIdAndChannel(tenantId, channel)
				.orElseThrow(() -> new ResourceNotFoundException("ChannelConfiguration", channel.name()));
		return ChannelConfigResponse.from(config, settingsCodec);
	}

	@Transactional
	public ChannelConfigResponse upsert(
			NotificationChannel channel,
			UpsertChannelConfigRequest request,
			UUID requestedTenantId,
			UserPrincipal actor
	) {
		UUID tenantId = resolveTenantId(requestedTenantId, actor);
		Tenant tenant = tenantRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

		Map<String, String> settings = request.settings() == null ? Map.of() : request.settings();
		settingsCodec.validateForEnable(channel, Boolean.TRUE.equals(request.enabled()), settings);

		ChannelConfiguration config = channelRepository.findByTenantIdAndChannel(tenantId, channel)
				.orElseGet(() -> newDisabled(tenant, channel));

		config.setEnabled(Boolean.TRUE.equals(request.enabled()));
		config.setProvider(blankToNull(request.provider()));
		config.setSettingsJson(settingsCodec.encode(settings));

		return ChannelConfigResponse.from(channelRepository.save(config), settingsCodec);
	}

	@Transactional
	public ChannelConfigResponse toggle(
			NotificationChannel channel,
			ToggleChannelRequest request,
			UUID requestedTenantId,
			UserPrincipal actor
	) {
		UUID tenantId = resolveTenantId(requestedTenantId, actor);
		ensureDefaults(tenantId);

		ChannelConfiguration config = channelRepository.findByTenantIdAndChannel(tenantId, channel)
				.orElseThrow(() -> new ResourceNotFoundException("ChannelConfiguration", channel.name()));

		Map<String, String> settings = settingsCodec.decode(config.getSettingsJson());
		settingsCodec.validateForEnable(channel, Boolean.TRUE.equals(request.enabled()), settings);
		config.setEnabled(Boolean.TRUE.equals(request.enabled()));

		return ChannelConfigResponse.from(channelRepository.save(config), settingsCodec);
	}

	private void ensureDefaults(UUID tenantId) {
		Tenant tenant = tenantRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
		Arrays.stream(NotificationChannel.values()).forEach(channel -> {
			if (!channelRepository.existsByTenantIdAndChannel(tenantId, channel)) {
				channelRepository.save(newDisabled(tenant, channel));
			}
		});
	}

	private ChannelConfiguration newDisabled(Tenant tenant, NotificationChannel channel) {
		ChannelConfiguration config = new ChannelConfiguration();
		config.setTenant(tenant);
		config.setChannel(channel);
		config.setEnabled(false);
		config.setSettingsJson("{}");
		return config;
	}

	private UUID resolveTenantId(UUID requestedTenantId, UserPrincipal actor) {
		if (actor.isTenantAdmin()) {
			if (requestedTenantId != null && !requestedTenantId.equals(actor.getTenantId())) {
				throw new ForbiddenException("Tenant admins can only manage their own channel configuration");
			}
			return actor.getTenantId();
		}
		if (!actor.isPlatformAdmin()) {
			throw new ForbiddenException("Insufficient permissions to manage channel configuration");
		}
		if (requestedTenantId == null) {
			throw new ApiException(
					ErrorCode.VALIDATION_FAILED,
					HttpStatus.BAD_REQUEST,
					"tenantId query parameter is required for platform admins"
			);
		}
		return requestedTenantId;
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
