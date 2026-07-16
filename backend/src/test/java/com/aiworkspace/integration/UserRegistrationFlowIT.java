package com.aiworkspace.integration;

import com.aiworkspace.shared.testcontainers.BaseIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow: register user → login → verify trial subscription → verify audit trail.
 *
 * Async listeners (@Async + @TransactionalEventListener) run on virtual threads.
 * Awaitility polls until the side-effects are observable in the DB, so this test is
 * not sensitive to listener scheduling latency.
 */
class UserRegistrationFlowIT extends BaseIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // JavaMailSender is mocked — no real SMTP needed in tests
    @MockBean JavaMailSender javaMailSender;

    private static final String EMAIL = "e2e-" + System.currentTimeMillis() + "@example.com";
    private static final String PASSWORD = "SecurePass123!";

    @Test
    void fullRegistrationAndLoginFlow() throws Exception {
        // Step 1: Register
        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "fullName": "Integration Test User"
                                }
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andReturn();

        JsonNode user = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        assertThat(user.get("data").get("id").asText()).isNotBlank();

        // Step 2: Login
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        JsonNode tokens = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = tokens.get("data").get("accessToken").asText();

        // Step 3: Verify trial subscription was created by BillingIamListener (async)
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() ->
                        mockMvc.perform(get("/api/v1/billing/subscription")
                                        .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("TRIAL"))
                );

        // Step 4: Verify audit event was recorded by IamAuditListener (async)
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    MvcResult auditResult = mockMvc.perform(get("/api/v1/audit/events")
                                    .header("Authorization", "Bearer " + accessToken))
                            .andExpect(status().isOk())
                            .andReturn();

                    JsonNode auditPage = objectMapper.readTree(auditResult.getResponse().getContentAsString());
                    JsonNode data = auditPage.get("data");
                    assertThat(data.get("totalElements").asInt()).isGreaterThanOrEqualTo(1);

                    JsonNode firstEvent = data.get("content").get(0);
                    assertThat(firstEvent.get("eventType").asText()).isEqualTo("USER_REGISTERED");
                });
    }

    @Test
    void register_returns409OnDuplicateEmail() throws Exception {
        String uniqueEmail = "dup-" + System.currentTimeMillis() + "@example.com";

        // First registration succeeds
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"pass123","fullName":"First"}
                                """.formatted(uniqueEmail)))
                .andExpect(status().isCreated());

        // Second registration with same email → 409
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"pass456","fullName":"Second"}
                                """.formatted(uniqueEmail)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns401WithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nobody@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/audit/events"))
                .andExpect(status().isUnauthorized());
    }
}
