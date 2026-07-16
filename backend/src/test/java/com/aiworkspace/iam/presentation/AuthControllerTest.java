package com.aiworkspace.iam.presentation;

import com.aiworkspace.iam.application.dto.AuthTokenDto;
import com.aiworkspace.iam.application.dto.UserDto;
import com.aiworkspace.iam.application.port.in.LoginUseCase;
import com.aiworkspace.iam.application.port.in.LogoutUseCase;
import com.aiworkspace.iam.application.port.in.RefreshAccessTokenUseCase;
import com.aiworkspace.iam.application.port.in.RegisterUserUseCase;
import com.aiworkspace.shared.exception.ConflictException;
import com.aiworkspace.shared.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RegisterUserUseCase registerUserUseCase;
    @MockBean LoginUseCase loginUseCase;
    @MockBean RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    @MockBean LogoutUseCase logoutUseCase;

    // Disables JWT filter for the web slice — tests focus on controller logic, not auth
    @Configuration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    void register_returns201WithUserData() throws Exception {
        // UserDto(String id, String email, String fullName, Set<String> roles, String status)
        UserDto userDto = new UserDto(UUID.randomUUID().toString(), "john@example.com",
                "John Doe", Set.of("USER"), "ACTIVE");
        when(registerUserUseCase.execute(any())).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "Secret123!",
                                  "fullName": "John Doe"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void register_returns409WhenEmailAlreadyExists() throws Exception {
        when(registerUserUseCase.execute(any())).thenThrow(new ConflictException("Email already registered"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"dup@example.com","password":"pass","fullName":"Dup"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200WithTokens() throws Exception {
        UserDto userDto = new UserDto(UUID.randomUUID().toString(), "john@example.com",
                "John Doe", Set.of("USER"), "ACTIVE");
        AuthTokenDto tokens = new AuthTokenDto("access-token", "refresh-token", userDto);
        when(loginUseCase.execute(any())).thenReturn(tokens);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "Secret123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_returns401OnInvalidCredentials() throws Exception {
        when(loginUseCase.execute(any())).thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"john@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
