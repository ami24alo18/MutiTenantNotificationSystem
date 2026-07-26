package com.multitenant.notification.auth;

import com.multitenant.notification.auth.dto.LoginRequest;
import com.multitenant.notification.auth.dto.LoginResponse;
import com.multitenant.notification.auth.dto.UserResponse;
import com.multitenant.notification.common.exception.UnauthorizedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserAccountRepository userAccountRepository;

	public AuthService(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			UserAccountRepository userAccountRepository
	) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userAccountRepository = userAccountRepository;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
			);
			UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
			UserAccount user = userAccountRepository.findById(principal.getId())
					.orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

			if (user.getTenant() != null && !user.getTenant().isActive()) {
				throw new UnauthorizedException("Tenant is inactive");
			}

			String token = jwtService.generateToken(user);
			return new LoginResponse(
					token,
					"Bearer",
					jwtService.getExpirationMs(),
					UserResponse.from(user)
			);
		}
		catch (AuthenticationException ex) {
			throw new UnauthorizedException("Invalid email or password");
		}
	}
}
