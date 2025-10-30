package com.example.cerpshashkin.service.security;

import com.example.cerpshashkin.dto.LoginRequest;
import com.example.cerpshashkin.dto.LoginResponse;
import com.example.cerpshashkin.dto.RegisterRequest;
import com.example.cerpshashkin.entity.RoleEntity;
import com.example.cerpshashkin.entity.UserEntity;
import com.example.cerpshashkin.exception.UserAlreadyExistsException;
import com.example.cerpshashkin.repository.RoleRepository;
import com.example.cerpshashkin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String LOG_REGISTER_START = "Registering new user: {}";
    private static final String LOG_REGISTER_SUCCESS = "User registered successfully: {}";
    private static final String LOG_LOGIN_START = "User login attempt: {}";
    private static final String LOG_LOGIN_SUCCESS = "User logged in successfully: {}";
    private static final String ERROR_EMAIL_EXISTS = "Email '%s' is already registered";
    private static final String ERROR_ROLE_NOT_FOUND = "Default role not found: %s";
    private static final String ERROR_INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public void register(final RegisterRequest request) {
        log.info(LOG_REGISTER_START, request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(
                    String.format(ERROR_EMAIL_EXISTS, request.email())
            );
        }

        final RoleEntity userRole = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new RuntimeException(
                        String.format(ERROR_ROLE_NOT_FOUND, ROLE_USER)
                ));

        final Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);

        final UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .createdAt(Instant.now())
                .roles(roles)
                .build();

        userRepository.save(user);
        log.info(LOG_REGISTER_SUCCESS, request.email());
    }

    public LoginResponse login(final LoginRequest request) {
        log.info(LOG_LOGIN_START, request.email());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            throw new BadCredentialsException(ERROR_INVALID_CREDENTIALS);
        }

        final String token = jwtService.generateToken(userDetails);

        final List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.info(LOG_LOGIN_SUCCESS, request.email());

        return LoginResponse.success(token, request.email(), roles);
    }
}
