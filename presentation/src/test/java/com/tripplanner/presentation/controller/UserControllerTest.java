package com.tripplanner.presentation.controller;

import com.tripplanner.business.service.UserService;
import com.tripplanner.domain.dto.UserResponseDTO;
import com.tripplanner.presentation.base.ControllerTestBase;
import com.tripplanner.presentation.config.SecurityConfig;
import com.tripplanner.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        ),
        properties = {
                "auth0.domain=test-domain.auth0.com",
                "auth0.clientId=test-client-id"
        }
)
@Import(GlobalExceptionHandler.class)
@DisplayName("UserController Tests (user-related endpoints with mocked UserService)")
class UserControllerTest extends ControllerTestBase {

    @MockBean
    private UserService userService;

    private UserResponseDTO existingUser;

    @BeforeEach
    void setUp() {
        existingUser = UserResponseDTO.builder()
                .korisnikId(42)
                .ime("Jane")
                .prezime("Doe")
                .email("jane.doe@example.com")
                .oauthProvider("auth0")
                .build();
    }

    
    private Jwt jwtFor(String email, String name, String sub, String picture) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject(sub)
                .claim("email_verified", true);
        if (email != null) {
            builder.claim("email", email);
        }
        if (name != null) {
            builder.claim("name", name);
        }
        if (picture != null) {
            builder.claim("picture", picture);
        }
        return builder.build();
    }

    @Nested
    @DisplayName("GET /api/auth/me - retrieve current user")
    class GetCurrentUserEndpoint {

        @Test
        @DisplayName("getCurrentUser_withExistingUser_returns200OkAndSerializedUser")
        void getCurrentUser_withExistingUser_returns200OkAndSerializedUser() throws Exception {
            
            
            String email = "jane.doe@example.com";
            String name = "Jane Doe";
            String sub = "auth0|abc123";
            String picture = "https://example.com/jane.jpg";

            when(userService.findOrCreateUserFromAuth0(email, name, sub, picture))
                    .thenReturn(existingUser);

            Jwt jwt = jwtFor(email, name, sub, picture);

            
            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(jwt))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.korisnikId").value(42))
                    .andExpect(jsonPath("$.sub").value(sub))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.name").value(name))
                    .andExpect(jsonPath("$.picture").value(picture))
                    .andExpect(jsonPath("$.email_verified").value(true));

            verify(userService, times(1))
                    .findOrCreateUserFromAuth0(email, name, sub, picture);
        }

        @Test
        @DisplayName("getCurrentUser_withNewUser_returns200OkAndAutoCreatedUser")
        void getCurrentUser_withNewUser_returns200OkAndAutoCreatedUser() throws Exception {
            
            String email = "newbie@example.com";
            String name = "New Bie";
            String sub = "auth0|new-user-999";
            String picture = "https://example.com/newbie.jpg";

            UserResponseDTO created = UserResponseDTO.builder()
                    .korisnikId(101)
                    .ime("New")
                    .prezime("Bie")
                    .email(email)
                    .oauthProvider("auth0")
                    .build();
            when(userService.findOrCreateUserFromAuth0(email, name, sub, picture))
                    .thenReturn(created);

            
            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(jwtFor(email, name, sub, picture)))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.korisnikId").value(101))
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.sub").value(sub))
                    .andExpect(jsonPath("$.name").value(name));

            verify(userService).findOrCreateUserFromAuth0(email, name, sub, picture);
        }

        @Test
        @DisplayName("getCurrentUser_passesJwtClaimsToServiceUnchanged")
        void getCurrentUser_passesJwtClaimsToServiceUnchanged() throws Exception {
            
            String email = "verbatim@example.com";
            String name = "Ver Batim";
            String sub = "auth0|verbatim";
            String picture = "https://example.com/v.jpg";

            when(userService.findOrCreateUserFromAuth0(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(existingUser);

            
            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(jwtFor(email, name, sub, picture))))
                    .andExpect(status().isOk());

            
            verify(userService).findOrCreateUserFromAuth0(
                    eq(email), eq(name), eq(sub), eq(picture));
        }

        @Test
        @DisplayName("getCurrentUser_withNullPictureClaim_returns200OkAndNullPicture")
        void getCurrentUser_withNullPictureClaim_returns200OkAndNullPicture() throws Exception {
            
            String email = "nopic@example.com";
            String name = "No Pic";
            String sub = "auth0|nopic";

            when(userService.findOrCreateUserFromAuth0(email, name, sub, null))
                    .thenReturn(existingUser);

            
            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(jwtFor(email, name, sub, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.picture").doesNotExist());

            verify(userService).findOrCreateUserFromAuth0(email, name, sub, null);
        }

        @Test
        @DisplayName("getCurrentUser_whenServiceThrowsRuntimeException_returns500InternalServerError")
        void getCurrentUser_whenServiceThrowsRuntimeException_returns500InternalServerError() throws Exception {
            
            when(userService.findOrCreateUserFromAuth0(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Database unavailable"));

            
            mockMvc.perform(get("/api/auth/me")
                            .with(jwt().jwt(jwtFor("a@b.com", "A B", "auth0|x", null))))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Database unavailable"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("Authorization on /api/auth/me")
    class AuthorizationTests {

        @Test
        @DisplayName("getCurrentUser_withoutJwt_doesNotReturn200")
        void getCurrentUser_withoutJwt_doesNotReturn200() throws Exception {
            
            
            
            
            
            
            
            mockMvc.perform(get("/api/auth/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status >= 200 && status < 300) {
                            throw new AssertionError(
                                    "Expected non-2xx response for unauthenticated request but got " + status);
                        }
                    });

            
            verify(userService, never())
                    .findOrCreateUserFromAuth0(any(), any(), any(), any());
        }
    }
}
