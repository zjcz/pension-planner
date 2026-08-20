package com.pensionplanner.income;

import com.pensionplanner.audit.StatePensionAudit;
import com.pensionplanner.audit.StatePensionAuditRepository;
import com.pensionplanner.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:pensionflow?mode=memory&cache=shared")
@AutoConfigureMockMvc
class StatePensionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StatePensionRepository statePensionRepository;

    @Autowired
    StatePensionAuditRepository statePensionAuditRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void getReturnsNullWhenNotCreated() throws Exception {
        Cookie cookie = register("sp_alice", "password123");

        mockMvc.perform(get("/api/v1/state-pension").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void upsertCreatesAndAudits() throws Exception {
        Cookie cookie = register("sp_bob", "password123");

        mockMvc.perform(put("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"yearlyAmount\":10000,\"takesEffectYear\":2026}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearlyAmount").value(10000))
                .andExpect(jsonPath("$.takesEffectYear").value(2026));

        Long userId = getUserId("sp_bob");
        assertThat(statePensionAuditRepository.findAll().stream()
                .filter(a -> a.getUserId() != null && a.getUserId().equals(userId))
                .map(StatePensionAudit::getAction)
                .toList()).containsExactly("CREATE");
    }

    @Test
    void upsertIsIdempotentUpdatesAndAudits() throws Exception {
        Cookie cookie = register("sp_carol", "password123");

        mockMvc.perform(put("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"yearlyAmount\":10000,\"takesEffectYear\":2026}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"yearlyAmount\":12000,\"takesEffectYear\":2030}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearlyAmount").value(12000))
                .andExpect(jsonPath("$.takesEffectYear").value(2030));

        Long userId = getUserId("sp_carol");
        assertThat(statePensionAuditRepository.findAll().stream()
                .filter(a -> a.getUserId() != null && a.getUserId().equals(userId))
                .map(StatePensionAudit::getAction)
                .toList()).containsExactly("CREATE", "UPDATE");

        assertThat(statePensionRepository.findByUserId(userId).orElseThrow().getYearlyAmount()).isEqualTo(12000L);
    }

    @Test
    void validationRejectsBadRanges() throws Exception {
        Cookie cookie = register("sp_dave", "password123");

        mockMvc.perform(put("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"yearlyAmount\":-1,\"takesEffectYear\":2026}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statePensionsAreIsolatedPerUser() throws Exception {
        Cookie alice = register("sp_eve", "password123");
        Cookie bob = register("sp_frank", "password123");

        mockMvc.perform(put("/api/v1/state-pension")
                        .cookie(alice)
                        .contentType("application/json")
                        .content("{\"yearlyAmount\":10000,\"takesEffectYear\":2026}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/state-pension").cookie(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    private Cookie register(String username, String password) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        return new Cookie("pp_jwt", cookieValueFrom(response, "pp_jwt"));
    }

    private Long getUserId(String username) {
        return userRepository.findByUsername(username).orElseThrow().getUserId();
    }

    private static String cookieValueFrom(MockHttpServletResponse response, String name) {
        String header = response.getHeader("Set-Cookie");
        for (String part : header.split(";")) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                return keyValue[1];
            }
        }
        throw new IllegalStateException("Cookie " + name + " not found in " + header);
    }
}
