package com.multitenant.notification.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Test
	void generateAndParseTokenRoundTrip() {
		UserAccount admin = userAccountRepository.findByEmailIgnoreCase("platform.admin@system.local")
				.orElseThrow();

		String token = jwtService.generateToken(admin);
		var claims = jwtService.parseClaims(token);

		assertThat(jwtService.extractUserId(claims)).isEqualTo(admin.getId());
		assertThat(claims.get("email", String.class)).isEqualTo(admin.getEmail());
		assertThat(claims.get("role", String.class)).isEqualTo(Role.PLATFORM_ADMIN.name());
	}
}
