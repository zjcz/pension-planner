package dev.jonclarke.pensionplanner.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:corsdefault?mode=memory&cache=shared")
@AutoConfigureMockMvc
class CorsDefaultIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void crossOriginPreflightIsRejectedByDefault() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}