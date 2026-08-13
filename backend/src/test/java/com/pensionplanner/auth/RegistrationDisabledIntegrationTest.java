package com.pensionplanner.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "DATABASE_PATH=file:regdis?mode=memory&cache=shared",
        "ALLOW_REGISTRATION=false"
})
@AutoConfigureMockMvc
class RegistrationDisabledIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void registerIsRejectedWhenDisabled() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void configReportsRegistrationDisabled() throws Exception {
        mockMvc.perform(get("/api/v1/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowRegistration").value(false));
    }
}
