package dev.jonclarke.pensionplanner.income;

import dev.jonclarke.pensionplanner.audit.OtherIncomeAudit;
import dev.jonclarke.pensionplanner.audit.OtherIncomeAuditRepository;
import dev.jonclarke.pensionplanner.user.UserRepository;
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
class OtherIncomeControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OtherIncomeRepository otherIncomeRepository;

    @Autowired
    OtherIncomeAuditRepository otherIncomeAuditRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void listReturnsEmptyWhenNothingCreated() throws Exception {
        Cookie cookie = register("oi_alice", "password123");

        mockMvc.perform(get("/api/v1/other-income").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createAndGetListUpdateDelete() throws Exception {
        Cookie cookie = register("oi_bob", "password123");
        Long userId = getUserId("oi_bob");

        // Create
        String createResponse = mockMvc.perform(post("/api/v1/other-income")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"Rental\",\"annualAmount\":6000,\"notes\":\"Flat income\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Rental"))
                .andExpect(jsonPath("$.annualAmount").value(6000))
                .andReturn().getResponse().getContentAsString();

        Long createdId = ((Number) com.jayway.jsonpath.JsonPath.read(createResponse, "$.id")).longValue();

        // List
        mockMvc.perform(get("/api/v1/other-income").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Rental"));

        // Get by ID
        mockMvc.perform(get("/api/v1/other-income/" + createdId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annualAmount").value(6000));

        // Update
        mockMvc.perform(put("/api/v1/other-income/" + createdId)
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"Rental\",\"annualAmount\":8000,\"notes\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annualAmount").value(8000));

        // Delete
        mockMvc.perform(delete("/api/v1/other-income/" + createdId).cookie(cookie))
                .andExpect(status().isNoContent());

        // List empty again
        mockMvc.perform(get("/api/v1/other-income").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Audit trail
        assertThat(otherIncomeAuditRepository.findAll().stream()
                .filter(a -> a.getUserId() != null && a.getUserId().equals(userId))
                .map(OtherIncomeAudit::getAction)
                .toList()).containsExactly("CREATE", "UPDATE", "DELETE");
    }

    @Test
    void validationRejectsBlankName() throws Exception {
        Cookie cookie = register("oi_carol", "password123");

        mockMvc.perform(post("/api/v1/other-income")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"annualAmount\":1000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void otherIncomeIsIsolatedPerUser() throws Exception {
        Cookie alice = register("oi_dave", "password123");
        Cookie bob = register("oi_eve", "password123");

        mockMvc.perform(post("/api/v1/other-income")
                        .cookie(alice)
                        .contentType("application/json")
                        .content("{\"name\":\"Alice Income\",\"annualAmount\":5000}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/other-income").cookie(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getReturns404ForOtherUsersItem() throws Exception {
        Cookie alice = register("oi_frank", "password123");
        Cookie bob = register("oi_grace", "password123");

        String createResponse = mockMvc.perform(post("/api/v1/other-income")
                        .cookie(alice)
                        .contentType("application/json")
                        .content("{\"name\":\"Alice Income\",\"annualAmount\":5000}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long aliceId = ((Number) com.jayway.jsonpath.JsonPath.read(createResponse, "$.id")).longValue();

        mockMvc.perform(get("/api/v1/other-income/" + aliceId).cookie(bob))
                .andExpect(status().isNotFound());
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
