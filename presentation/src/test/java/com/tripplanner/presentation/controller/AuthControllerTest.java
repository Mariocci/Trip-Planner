package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.UserService;
import com.tripplanner.domain.dto.UserResponseDTO;
import com.tripplanner.presentation.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link AuthController}.
 *
 * <p>Verifies HTTP request handling, status codes, request/response JSON
 * serialization, and interactions with the underlying service layer.</p>
 *
 * <p>Note: The actual {@link AuthController} exposes the following endpoints:
 * <ul>
 *   <li>GET {@code /api/auth/config} - Returns Auth0 configuration</li>
 *   <li>GET {@code /api/auth/me} - Returns current authenticated user (auto-creates from Auth0)</li>
 *   <li>POST {@code /api/auth/logout} - Returns logout response</li>
 * </ul>
 * The task description references OAuth {@code initiate}/{@code callback} endpoints
 * which are handled at the Auth0 layer rather than via custom REST endpoints in this
 * application; therefore the tests below cover the endpoints actually exposed by
 * {@code AuthController}, exercising the behaviours required by Requirements
 * 3.1, 3.9, 3.10, 3.11, 3.12, 3.13.</p>
 *
 * <p>The {@code /api/auth/**} path is configured as {@code permitAll} in
 * {@link SecurityConfig}, but the {@code /me} endpoint requires a {@link
 * org.springframework.security.oauth2.jwt.Jwt} principal because of
 * {@code @AuthenticationPrincipal} - the test provides this via
 * {@code SecurityMockMvcRequestPostProcessors#jwt()}.</p>
 */
@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "auth0.domain=test-tenant.auth0.com",
        "auth0.clientId=test-client-id",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test-tenant.auth0.com/"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // The Spring Security OAuth2 Resource Server pulls in a JwtDecoder bean
    // when an issuer-uri is configured. To avoid the test attempting to
    // contact the real Auth0 issuer for JWKS, we mock it.
    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    // ------------------------------------------------------------------
    // GET /api/auth/config
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/auth/config")
    class GetAuthConfigTests {

        @Test
        @DisplayName("returns 200 OK with Auth0 configuration JSON")
        void getAuthConfig_whenInvoked_returnsOkWithAuth0Configuration() throws Exception {
            mockMvc.perform(get("/api/auth/config")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.domain").value("test-tenant.auth0.com"))
                    .andExpect(jsonPath("$.clientId").value("test-client-id"))
                    .andExpect(jsonPath("$.audience").value("https://test-tenant.auth0.com/api/v2/"))
                    .andExpect(jsonPath("$.redirectUri").value("http://localhost:5173/callback"));
        }

        @Test
        @DisplayName("does not interact with UserService")
        void getAuthConfig_whenInvoked_doesNotCallUserService() throws Exception {
            mockMvc.perform(get("/api/auth/config"))
                    .andExpect(status().isOk());

            verifyNoInteractions(userService);
        }
    }

    // ------------------------------------------------------------------
    // GET /api/auth/me
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserTests {

        @Test
        @DisplayName("returns 200 OK with user details when JWT is present")
        void getCurrentUser_withValidJwt_returnsOkWithUserInfo() throws Exception {
            // Given
            UserResponseDTO existingUser = UserResponseDTO.builder()
                    .korisnikId(42)
                    .ime("Jane")
                    .prezime("Doe")
                    .email("jane.doe@example.com")
                    .oauthProvider("auth0")
                    .build();

            when(userService.findOrCreateUserFromAuth0(
                    eq("jane.doe@example.com"),
                    eq("Jane Doe"),
                    eq("auth0|abc123"),
                    eq("https://example.com/avatar.png")))
                    .thenReturn(existingUser);

            // When / Then
            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(builder -> builder
                                    .subject("auth0|abc123")
                                    .claim("email", "jane.doe@example.com")
                                    .claim("name", "Jane Doe")
                                    .claim("picture", "https://example.com/avatar.png")
                                    .claim("email_verified", true))))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.korisnikId").value(42))
                    .andExpect(jsonPath("$.sub").value("auth0|abc123"))
                    .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                    .andExpect(jsonPath("$.name").value("Jane Doe"))
                    .andExpect(jsonPath("$.picture").value("https://example.com/avatar.png"))
                    .andExpect(jsonPath("$.email_verified").value(true));

            verify(userService, times(1)).findOrCreateUserFromAuth0(
                    "jane.doe@example.com",
                    "Jane Doe",
                    "auth0|abc123",
                    "https://example.com/avatar.png");
        }

        @Test
        @DisplayName("auto-creates user when none exists for the JWT subject")
        void getCurrentUser_whenUserDoesNotExist_invokesFindOrCreateAndReturnsCreatedUser() throws Exception {
            // Given - simulate findOrCreate returning a freshly-created user
            UserResponseDTO createdUser = UserResponseDTO.builder()
                    .korisnikId(101)
                    .ime("New")
                    .prezime("User")
                    .email("new.user@example.com")
                    .oauthProvider("auth0")
                    .build();

            when(userService.findOrCreateUserFromAuth0(
                    eq("new.user@example.com"),
                    eq("New User"),
                    eq("auth0|new-sub"),
                    eq("https://example.com/new.png")))
                    .thenReturn(createdUser);

            // When / Then
            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(builder -> builder
                                    .subject("auth0|new-sub")
                                    .claim("email", "new.user@example.com")
                                    .claim("name", "New User")
                                    .claim("picture", "https://example.com/new.png")
                                    .claim("email_verified", false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.korisnikId").value(101))
                    .andExpect(jsonPath("$.sub").value("auth0|new-sub"))
                    .andExpect(jsonPath("$.email").value("new.user@example.com"))
                    .andExpect(jsonPath("$.email_verified").value(false));

            verify(userService).findOrCreateUserFromAuth0(
                    "new.user@example.com",
                    "New User",
                    "auth0|new-sub",
                    "https://example.com/new.png");
        }

        @Test
        @DisplayName("does not invoke UserService when JWT principal is absent")
        void getCurrentUser_withoutJwt_doesNotInvokeUserService() throws Exception {
            // Note: SecurityConfig declares /api/auth/** as permitAll, so the security
            // filter chain does NOT short-circuit unauthenticated requests with 401.
            // The actual JWT-based 401 behaviour is exercised by full-stack integration
            // tests where the OAuth2 resource server is wired against a real issuer.
            // Here we only assert that without a Jwt principal the request never
            // reaches UserService.findOrCreateUserFromAuth0(...).
            mockMvc.perform(get("/api/auth/me")
                    .contentType(MediaType.APPLICATION_JSON));

            verify(userService, never())
                    .findOrCreateUserFromAuth0(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("propagates null optional claims (email_verified missing)")
        void getCurrentUser_withMissingEmailVerifiedClaim_returnsNullForThatField() throws Exception {
            UserResponseDTO user = UserResponseDTO.builder()
                    .korisnikId(7)
                    .ime("No")
                    .prezime("Claim")
                    .email("no.claim@example.com")
                    .oauthProvider("auth0")
                    .build();

            when(userService.findOrCreateUserFromAuth0(
                    eq("no.claim@example.com"),
                    eq("No Claim"),
                    eq("auth0|7"),
                    eq("https://example.com/x.png")))
                    .thenReturn(user);

            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(builder -> builder
                                    .subject("auth0|7")
                                    .claim("email", "no.claim@example.com")
                                    .claim("name", "No Claim")
                                    .claim("picture", "https://example.com/x.png"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.korisnikId").value(7))
                    .andExpect(jsonPath("$.email_verified").doesNotExist());
        }
    }

    // ------------------------------------------------------------------
    // POST /api/auth/logout
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("returns 200 OK with logout message and Auth0 logout URL")
        void logout_whenInvoked_returnsOkWithLogoutPayload() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Logged out successfully"))
                    .andExpect(jsonPath("$.logoutUrl").value(
                            "https://test-tenant.auth0.com/v2/logout?client_id=test-client-id&returnTo=http://localhost:5173"));
        }

        @Test
        @DisplayName("does not require authentication")
        void logout_withoutAuthentication_returnsOk() throws Exception {
            // /api/auth/** is permitAll in SecurityConfig, so logout must succeed without a JWT.
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk());

            verifyNoInteractions(userService);
        }
    }
}
