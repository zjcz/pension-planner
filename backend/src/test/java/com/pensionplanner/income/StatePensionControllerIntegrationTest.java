package com.pensionplanner.income;

import com.pensionplanner.audit.StatePensionAudit;
import com.pensionplanner.audit.StatePensionAuditRepository;
import com.pensionplanner.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void listReturnsEmptyWhenNothingCreated() throws Exception {
        Cookie cookie = register("sp_alice", "password123");

        mockMvc.perform(get("/api/v1/state-pension").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createPersistsAndAudits() throws Exception {
        Cookie cookie = register("sp_bob", "password123");

        mockMvc.perform(post("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"My Pension\",\"yearlyAmount\":10000,\"takesEffectYear\":2026}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Pension"))
                .andExpect(jsonPath("$.yearlyAmount").value(10000))
                .andExpect(jsonPath("$.takesEffectYear").value(2026));

        Long userId = getUserId("sp_bob");
        assertThat(statePensionAuditRepository.findAll().stream()
                .filter(a -> a.getUserId() != null && a.getUserId().equals(userId))
                .map(StatePensionAudit::getAction)
                .toList()).containsExactly("CREATE");
        assertThat(statePensionRepository.findByUserIdOrderByNameAsc(userId)).hasSize(1);
    }

    @Test
    void createAllowsMultipleRecordsPerUser() throws Exception {
        Cookie cookie = register("sp_multi", "password123");
        Long userId = getUserId("sp_multi");

        mockMvc.perform(post("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"Mine\",\"yearlyAmount\":11000,\"takesEffectYear\":2040}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"Partner\",\"yearlyAmount\":7000,\"takesEffectYear\":2042}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/state-pension").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        assertThat(statePensionRepository.findByUserIdOrderByNameAsc(userId)).hasSize(2);
    }

    @Test
    void updateChangesAndAudits() throws Exception {
        Cookie cookie = register("sp_carol", "password123");

        String id = createStatePension(cookie, "First", 10000, 2026);

        mockMvc.perform(put("/api/v1/state-pension/" + id)
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"Renamed\",\"yearlyAmount\":12000,\"takesEffectYear\":2030}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.yearlyAmount").value(12000))
                .andExpect(jsonPath("$.takesEffectYear").value(2030));

        Long userId = getUserId("sp_carol");
        assertThat(statePensionAuditRepository.findAll().stream()
                .filter(a -> a.getUserId() != null && a.getUserId().equals(userId))
                .map(StatePensionAudit::getAction)
                .toList()).containsExactly("CREATE", "UPDATE");

        assertThat(statePensionRepository.findByIdAndUserId(Long.valueOf(id), userId).orElseThrow().getYearlyAmount()).isEqualTo(12000L);
    }

    @Test
    void deleteRemovesAndAudits() throws Exception {
        Cookie cookie = register("sp_dave", "password123");
        Long userId = getUserId("sp_dave");

        String id = createStatePension(cookie, "Doomed", 9000, 2028);

        mockMvc.perform(delete("/api/v1/state-pension/" + id).cookie(cookie))
                .andExpect(status().isNoContent());

        assertThat(statePensionRepository.findByIdAndUserId(Long.valueOf(id), userId)).isEmpty();
        assertThat(statePensionAuditRepository.findAll().stream()
                .filter(a -> a.getUserId() != null && a.getUserId().equals(userId))
                .map(StatePensionAudit::getAction)
                .toList()).containsExactly("CREATE", "DELETE");
    }

    @Test
    void validationRejectsBadRanges() throws Exception {
        Cookie cookie = register("sp_erin", "password123");

        mockMvc.perform(post("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"X\",\"yearlyAmount\":-1,\"takesEffectYear\":2026}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"yearlyAmount\":1000,\"takesEffectYear\":2026}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statePensionsAreIsolatedPerUser() throws Exception {
        Cookie alice = register("sp_eve", "password123");
        Cookie bob = register("sp_frank", "password123");

        mockMvc.perform(post("/api/v1/state-pension")
                        .cookie(alice)
                        .contentType("application/json")
                        .content("{\"name\":\"Alice SP\",\"yearlyAmount\":10000,\"takesEffectYear\":2026}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/state-pension").cookie(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private String createStatePension(Cookie cookie, String name, int yearlyAmount, int takesEffectYear) throws Exception {
        return JsonPath.read(mockMvc.perform(post("/api/v1/state-pension")
                .cookie(cookie)
                .contentType("application/json")
                .content("{\"name\":\"" + name + "\",\"yearlyAmount\":" + yearlyAmount
                        + ",\"takesEffectYear\":" + takesEffectYear + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id").toString();
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