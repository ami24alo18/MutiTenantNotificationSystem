package com.multitenant.notification.auth;

import com.multitenant.notification.auth.dto.CreateUserRequest;
import com.multitenant.notification.auth.dto.UpdateUserRequest;
import com.multitenant.notification.auth.dto.UserResponse;
import com.multitenant.notification.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAnyRole('PLATFORM_ADMIN', 'TENANT_ADMIN')")
public class UserController {

	private final UserAccountService userAccountService;

	public UserController(UserAccountService userAccountService) {
		this.userAccountService = userAccountService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("User created", userAccountService.create(request, actor));
	}

	@GetMapping
	public ApiResponse<List<UserResponse>> list() {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(userAccountService.list(actor));
	}

	@GetMapping("/{id}")
	public ApiResponse<UserResponse> get(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok(userAccountService.getById(id, actor));
	}

	@PutMapping("/{id}")
	public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		return ApiResponse.ok("User updated", userAccountService.update(id, request, actor));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable UUID id) {
		UserPrincipal actor = SecurityUtils.requireCurrentUser();
		userAccountService.delete(id, actor);
		return ApiResponse.okMessage("User deactivated");
	}
}
