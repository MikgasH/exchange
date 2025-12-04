package com.example.cerpshashkin.controller;

import com.example.cerpshashkin.BaseWireMockTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class AuthControllerIntegrationTest extends BaseWireMockTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private RestClient restClient;

    @Test
    void register_WithValidData_ShouldCreateUser() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .build();

        String response = restClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .body(String.class);

        assertThat(response).contains("User registered successfully");
        assertThat(userRepository.existsByEmail("newuser@example.com")).isTrue();
    }

    @Test
    void register_WithExistingEmail_ShouldReturnConflict() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .password("Password123!")
                .build();

        restClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("User already exists")
                            .contains("Email 'user@example.com' is already registered");
                })
                .toBodilessEntity();
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("Password123!")
                .build();

        restClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Validation error")
                            .contains("Invalid email format");
                })
                .toBodilessEntity();
    }

    @Test
    void register_WithWeakPassword_ShouldReturnBadRequest() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("weak")
                .build();

        restClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Validation error");
                })
                .toBodilessEntity();
    }

    @Test
    void register_WithPasswordWithoutUppercase_ShouldReturnBadRequest() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123!")
                .build();

        restClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Validation error")
                            .contains("Password must contain at least one digit, one lowercase, one uppercase letter and one special character");
                })
                .toBodilessEntity();
    }

    @Test
    void register_WithPasswordWithoutSpecialChar_ShouldReturnBadRequest() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123")
                .build();

        restClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body).contains("Validation error");
                })
                .toBodilessEntity();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("password")
                .build();

        String response = restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .body(String.class);

        assertThat(response)
                .contains("\"token\":")
                .contains("\"type\":\"Bearer\"")
                .contains("\"email\":\"user@example.com\"")
                .contains("\"roles\":[\"ROLE_USER\"]");
    }

    @Test
    void login_WithInvalidPassword_ShouldReturnUnauthorized() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("wrongpassword")
                .build();

        restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .onStatus(status -> status.value() == 401, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body)
                            .contains("Authentication failed")
                            .contains("Invalid username or password");
                })
                .toBodilessEntity();
    }

    @Test
    void login_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("Password123!")
                .build();

        restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .onStatus(status -> status.value() == 401, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body).contains("Authentication failed");
                })
                .toBodilessEntity();
    }

    @Test
    void login_WithPremiumUser_ShouldReturnMultipleRoles() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("premium@example.com")
                .password("password")
                .build();

        String response = restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .body(String.class);

        assertThat(response)
                .contains("\"token\":")
                .contains("\"email\":\"premium@example.com\"")
                .contains("ROLE_USER")
                .contains("ROLE_PREMIUM_USER");
    }

    @Test
    void login_WithAdminUser_ShouldReturnAllRoles() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("admin@example.com")
                .password("password")
                .build();

        String response = restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(request))
                .retrieve()
                .body(String.class);

        assertThat(response)
                .contains("\"token\":")
                .contains("\"email\":\"admin@example.com\"")
                .contains("ROLE_USER")
                .contains("ROLE_PREMIUM_USER")
                .contains("ROLE_ADMIN");
    }

    @Test
    void login_WithMissingEmail_ShouldReturnBadRequest() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        String requestBody = "{\"password\":\"Password123!\"}";

        restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body).contains("Validation error");
                })
                .toBodilessEntity();
    }

    @Test
    void login_WithMissingPassword_ShouldReturnBadRequest() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        String requestBody = "{\"email\":\"user@example.com\"}";

        restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, httpResponse) -> {
                    String body = new String(httpResponse.getBody().readAllBytes());
                    assertThat(body).contains("Validation error");
                })
                .toBodilessEntity();
    }

    @Test
    void fullWorkflow_RegisterAndLogin_ShouldWorkCorrectly() throws Exception {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port + "/api/v1/auth")
                .build();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("workflow@example.com")
                .password("Workflow123!")
                .build();

        String registerResponse = restClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(registerRequest))
                .retrieve()
                .body(String.class);

        assertThat(registerResponse).contains("User registered successfully");

        LoginRequest loginRequest = LoginRequest.builder()
                .email("workflow@example.com")
                .password("Workflow123!")
                .build();

        String loginResponse = restClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginRequest))
                .retrieve()
                .body(String.class);

        assertThat(loginResponse)
                .contains("\"token\":")
                .contains("\"email\":\"workflow@example.com\"")
                .contains("ROLE_USER");
    }
}
