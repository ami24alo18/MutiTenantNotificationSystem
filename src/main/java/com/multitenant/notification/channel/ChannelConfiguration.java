package com.multitenant.notification.channel;

import com.multitenant.notification.tenant.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "channel_configurations",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_channel_config_tenant_channel",
				columnNames = {"tenant_id", "channel"}
		)
)
public class ChannelConfiguration {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private NotificationChannel channel;

	@Column(nullable = false)
	private boolean enabled = false;

	@Column(length = 100)
	private String provider;

	@Column(name = "settings_json", nullable = false, length = 4000)
	private String settingsJson = "{}";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (id == null) {
			id = UUID.randomUUID();
		}
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public NotificationChannel getChannel() {
		return channel;
	}

	public void setChannel(NotificationChannel channel) {
		this.channel = channel;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getSettingsJson() {
		return settingsJson;
	}

	public void setSettingsJson(String settingsJson) {
		this.settingsJson = settingsJson;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
