package com.multitenant.notification.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a default platform admin exists for local/bootstrap access.
 */
@Component
public class PlatformAdminBootstrap implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(PlatformAdminBootstrap.class);

	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final PlatformAdminBootstrapProperties properties;

	public PlatformAdminBootstrap(
			UserAccountRepository userAccountRepository,
			PasswordEncoder passwordEncoder,
			PlatformAdminBootstrapProperties properties
	) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.properties = properties;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (userAccountRepository.existsByRole(Role.PLATFORM_ADMIN)) {
			return;
		}

		UserAccount admin = new UserAccount();
		admin.setEmail(properties.getEmail().trim().toLowerCase());
		admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
		admin.setFullName(properties.getFullName());
		admin.setRole(Role.PLATFORM_ADMIN);
		admin.setTenant(null);
		admin.setActive(true);
		userAccountRepository.save(admin);

		log.info("Bootstrapped platform admin user: {}", admin.getEmail());
	}
}
