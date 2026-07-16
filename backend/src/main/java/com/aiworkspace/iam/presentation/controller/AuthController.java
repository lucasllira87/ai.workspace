package com.aiworkspace.iam.presentation.controller;

import com.aiworkspace.iam.application.command.LoginCommand;
import com.aiworkspace.iam.application.command.LogoutCommand;
import com.aiworkspace.iam.application.command.RefreshTokenCommand;
import com.aiworkspace.iam.application.command.RegisterUserCommand;
import com.aiworkspace.iam.application.dto.AuthTokenDto;
import com.aiworkspace.iam.application.dto.UserDto;
import com.aiworkspace.iam.application.port.in.LoginUseCase;
import com.aiworkspace.iam.application.port.in.LogoutUseCase;
import com.aiworkspace.iam.application.port.in.RefreshAccessTokenUseCase;
import com.aiworkspace.iam.application.port.in.RegisterUserUseCase;
import com.aiworkspace.iam.presentation.request.LoginRequest;
import com.aiworkspace.iam.presentation.request.LogoutRequest;
import com.aiworkspace.iam.presentation.request.RefreshRequest;
import com.aiworkspace.iam.presentation.request.RegisterRequest;
import com.aiworkspace.iam.presentation.response.AuthResponse;
import com.aiworkspace.iam.presentation.response.UserResponse;
import com.aiworkspace.shared.presentation.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration, login, token management and logout")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                           LoginUseCase loginUseCase,
                           RefreshAccessTokenUseCase refreshAccessTokenUseCase,
                           LogoutUseCase logoutUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserDto user = registerUserUseCase.execute(
                new RegisterUserCommand(request.email(), request.password(), request.fullName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", toUserResponse(user)));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive access + refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthTokenDto tokens = loginUseCase.execute(new LoginCommand(
                request.email(),
                request.password(),
                request.deviceName(),
                request.deviceType(),
                httpRequest.getRemoteAddr()
        ));
        return ResponseEntity.ok(ApiResponse.ok(toAuthResponse(tokens)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a valid refresh token for a new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        AuthTokenDto tokens = refreshAccessTokenUseCase.execute(
                new RefreshTokenCommand(request.refreshToken(), httpRequest.getRemoteAddr()));
        return ResponseEntity.ok(ApiResponse.ok(toAuthResponse(tokens)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the given refresh token and end the session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            org.springframework.security.core.Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        logoutUseCase.execute(new LogoutCommand(request.refreshToken(), userId));
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    private UserResponse toUserResponse(UserDto dto) {
        return new UserResponse(dto.id(), dto.email(), dto.fullName(), dto.roles(), dto.status());
    }

    private AuthResponse toAuthResponse(AuthTokenDto dto) {
        return new AuthResponse(dto.accessToken(), dto.refreshToken(), toUserResponse(dto.user()));
    }
}
