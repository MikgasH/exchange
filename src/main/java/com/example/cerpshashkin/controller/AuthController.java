package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.dto.LoginRequest;
import com.example.cerpshashkin.dto.LoginResponse;
import com.example.cerpshashkin.dto.RegisterRequest;
import com.example.cerpshashkin.service.security.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and authentication endpoints")
public class AuthController {

    private static final String LOG_REGISTER_REQUEST = "POST /api/v1/auth/register - registering user: {}";
    private static final String LOG_REGISTER_SUCCESS = "User registered successfully: {}";
    private static final String LOG_LOGIN_REQUEST = "POST /api/v1/auth/login - login attempt for user: {}";
    private static final String LOG_LOGIN_SUCCESS = "User logged in successfully: {}";

    private static final String MESSAGE_REGISTER_SUCCESS = "User registered successfully";

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with ROLE_USER by default. "
                    + "Password must contain at least 8 characters, one digit, one uppercase letter, "
                    + "one lowercase letter, and one special character."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<String> register(@Valid @RequestBody final RegisterRequest request) {
        log.info(LOG_REGISTER_REQUEST, request.email());

        authenticationService.register(request);

        log.info(LOG_REGISTER_SUCCESS, request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(MESSAGE_REGISTER_SUCCESS);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticates user and returns JWT access token. "
                    + "Token must be included in subsequent requests as: Authorization: Bearer {token}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account disabled")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        log.info(LOG_LOGIN_REQUEST, request.email());

        final LoginResponse response = authenticationService.login(request);

        log.info(LOG_LOGIN_SUCCESS, request.email());
        return ResponseEntity.ok(response);
    }
}
