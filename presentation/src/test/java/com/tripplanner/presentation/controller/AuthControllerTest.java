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

    
    
    
    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    
    
    

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

    
    
    

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUserTests {

        @Test
        @DisplayName("returns 200 OK with user details when JWT is present")
        void getCurrentUser_withValidJwt_returnsOkWithUserInfo() throws Exception {
            
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
            
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk());

            verifyNoInteractions(userService);
        }
    }
}
