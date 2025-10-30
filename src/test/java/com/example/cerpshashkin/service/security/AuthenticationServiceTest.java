package com.example.cerpshashkin.service.security;

import com.example.cerpshashkin.dto.LoginRequest;
import com.example.cerpshashkin.dto.LoginResponse;
import com.example.cerpshashkin.dto.RegisterRequest;
import com.example.cerpshashkin.entity.RoleEntity;
import com.example.cerpshashkin.entity.UserEntity;
import com.example.cerpshashkin.exception.UserAlreadyExistsException;
import com.example.cerpshashkin.repository.RoleRepository;
import com.example.cerpshashkin.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_WithNewUser_ShouldCreateUserSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .build();

        RoleEntity roleUser = RoleEntity.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");

        authenticationService.register(request);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded_password");
        assertThat(savedUser.getEnabled()).isTrue();
        assertThat(savedUser.getRoles()).hasSize(1);
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email 'existing@example.com' is already registered");

        verify(userRepository, never()).save(any());
        verify(roleRepository, never()).findByName(anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_WhenRoleNotFound_ShouldThrowException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Default role not found: ROLE_USER");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("Password123!")
                .build();

        UserDetails userDetails = User.builder()
                .username("user@example.com")
                .password("encoded_password")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();

        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.password(), userDetails.getPassword())).thenReturn(true);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt_token_here");

        LoginResponse response = authenticationService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt_token_here");
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.roles()).hasSize(1);
        assertThat(response.roles()).contains("ROLE_USER");

        verify(userDetailsService, times(1)).loadUserByUsername(request.email());
        verify(passwordEncoder, times(1)).matches(request.password(), userDetails.getPassword());
        verify(jwtService, times(1)).generateToken(userDetails);
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("WrongPassword123!")
                .build();

        UserDetails userDetails = User.builder()
                .username("user@example.com")
                .password("encoded_password")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();

        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.password(), userDetails.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_WithMultipleRoles_ShouldReturnAllRoles() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@example.com")
                .password("Password123!")
                .build();

        UserDetails userDetails = User.builder()
                .username("admin@example.com")
                .password("encoded_password")
                .authorities(
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )
                )
                .build();

        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
        when(passwordEncoder.matches(request.password(), userDetails.getPassword())).thenReturn(true);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt_token_here");

        LoginResponse response = authenticationService.login(request);

        assertThat(response.roles()).hasSize(2);
        assertThat(response.roles()).contains("ROLE_USER", "ROLE_ADMIN");
    }
}
