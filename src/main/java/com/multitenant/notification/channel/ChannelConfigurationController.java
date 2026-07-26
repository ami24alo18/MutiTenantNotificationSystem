package com.multitenant.notification.channel;

import com.multitenant.notification.auth.SecurityUtils;
import com.multitenant.notification.auth.UserPrincipal;
import com.multitenant.notification.channel.dto.ChannelConfigResponse;
import com.multitenant.notification.channel.dto.ToggleChannelRequest;
import com.multitenant.notification.channel.dto.UpsertChannelConfigRequest;
import com.multitenant.notification.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
@PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'TENANT_ADMIN')")
public class ChannelConfigurationController {

	private final ChannelConfigurationService channelConfigurationService;

	public ChannelConfigurationController(ChannelConfigurationService channelConfigurationService) {
		this.channelConfigurationService = channelConfigurationService;
	}

	@GetMapping
	public ApiResponse<List<ChannelConfigResponse>> list(@RequestParam(required = false) UUID tenantId) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(channelConfigurationService.list(tenantId, actor));
	}

	@GetMapping("/{channel}")
	public ApiResponse<ChannelConfigResponse> get(
			@PathVariable NotificationChannel channel,
			@RequestParam(required = false) UUID tenantId
	) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(channelConfigurationService.get(channel, tenantId, actor));
	}

	@PutMapping("/{channel}")
	public ApiResponse<ChannelConfigResponse> upsert(
			@PathVariable NotificationChannel channel,
			@RequestParam(required = false) UUID tenantId,
			@Valid @RequestBody UpsertChannelConfigRequest request
	) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(
				"Channel configuration saved",
				channelConfigurationService.upsert(channel, request, tenantId, actor)
		);
	}

	@PutMapping("/{channel}/enabled")
	public ApiResponse<ChannelConfigResponse> toggle(
			@PathVariable NotificationChannel channel,
			@RequestParam(required = false) UUID tenantId,
			@Valid @RequestBody ToggleChannelRequest request
	) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(
				"Channel status updated",
				channelConfigurationService.toggle(channel, request, tenantId, actor)
		);
	}
}
