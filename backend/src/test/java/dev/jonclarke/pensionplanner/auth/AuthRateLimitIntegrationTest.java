package dev.jonclarke.pensionplanner.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "DATABASE_PATH=file:ratelimit?mode=memory&cache=shared",
        "pension-planner.rate-limit.enabled=true",
        "pension-planner.rate-limit.capacity=2",
        "pension-planner.rate-limit.refill-per-minute=1"
})
@AutoConfigureMockMvc
class AuthRateLimitIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    private static String loginBody() {
        return "{\"username\":\"nobody\",\"password\":\"wrongpassword\"}";
    }

    @Test
    void loginIsRateLimitedPerIp() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .header("X-Forwarded-For", "10.0.0.1")
                            .contentType(APPLICATION_JSON).content(loginBody()))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.0.0.1")
                        .contentType(APPLICATION_JSON).content(loginBody()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void registerHasItsOwnBudget() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/auth/register")
                            .header("X-Forwarded-For", "10.0.0.2")
                            .contentType(APPLICATION_JSON)
                            .content("{\"username\":\"ratelimit" + i + "\",\"password\":\"password123\"}"))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("X-Forwarded-For", "10.0.0.2")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"ratelimit2\",\"password\":\"password123\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void nonAuthEndpointsAreNotRateLimited() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/pensions")
                            .contentType(APPLICATION_JSON)
                            .content("{\"name\":\"x\",\"maturityDate\":\"2045-01-01\",\"status\":\"ACTIVE\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void honorsForwardedForHeader() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .header("X-Forwarded-For", "10.0.0.3")
                            .contentType(APPLICATION_JSON).content(loginBody()))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "10.0.0.3")
                        .contentType(APPLICATION_JSON).content(loginBody()))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(loginBody()))
                .andExpect(status().isUnauthorized());
    }
}