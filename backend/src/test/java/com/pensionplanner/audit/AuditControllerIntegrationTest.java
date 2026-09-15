package com.pensionplanner.audit;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:pensionflow?mode=memory&cache=shared")
@AutoConfigureMockMvc
class AuditControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void pensionAuditReturnsDescendingOrdersAndScopesToUser() throws Exception {
        Cookie cookie = register("audit_pension_alice", "password123");
        String pensionId = createPension(cookie, "P1");

        mockMvc.perform(put("/api/v1/pensions/" + pensionId)
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"P1-renamed\",\"maturityDate\":\"2046-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit/pensions/" + pensionId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].action").value("UPDATE"))
                .andExpect(jsonPath("$[1].action").value("CREATE"))
                .andExpect(jsonPath("$[0].name").value("P1"))
                .andExpect(jsonPath("$[1].name").value("P1"));
    }

    @Test
    void statementAuditAndOtherIncomeAuditReturnDescending() throws Exception {
        Cookie cookie = register("audit_stmt_bob", "password123");
        String pensionId = createPension(cookie, "P2");

        String statementId = createStatement(cookie, pensionId, "2026-01-01", "100000");
        mockMvc.perform(put("/api/v1/pensions/" + pensionId + "/statements/" + statementId)
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"2026-06-01\",\"planValue\":150000,"
                                + "\"projectedAnnualAmount\":7500}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit/statements/" + statementId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].action").value("UPDATE"))
                .andExpect(jsonPath("$[1].action").value("CREATE"))
                .andExpect(jsonPath("$[0].planValue").value(100000))
                .andExpect(jsonPath("$[1].planValue").value(100000));

        String oiId = createOtherIncome(cookie, "Rent");
        mockMvc.perform(put("/api/v1/other-income/" + oiId)
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Rent2\",\"annualAmount\":70000}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit/other-income/" + oiId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].action").value("UPDATE"))
                .andExpect(jsonPath("$[1].action").value("CREATE"));
    }

    @Test
    void statePensionAuditReturnsDescendingAndScopesToUser() throws Exception {
        Cookie alice = register("audit_sp_alice", "password123");
        Cookie bob = register("audit_sp_bob", "password123");

        String spId = createStatePension(alice, "State");
        mockMvc.perform(put("/api/v1/state-pension/" + spId)
                        .cookie(alice)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"State2\",\"yearlyAmount\":12000,\"takesEffectYear\":2030}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/audit/state-pensions/" + spId).cookie(alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].action").value("UPDATE"))
                .andExpect(jsonPath("$[1].action").value("CREATE"))
                .andExpect(jsonPath("$[0].yearlyAmount").value(11000));

        mockMvc.perform(get("/api/v1/audit/state-pensions/" + spId).cookie(bob))
                .andExpect(status().isNotFound());
    }

    @Test
    void auditViewRejectsOtherUsersRecords() throws Exception {
        Cookie alice = register("audit_owner_alice", "password123");
        Cookie bob = register("audit_other_bob", "password123");
        String pensionId = createPension(alice, "P-alice");
        String statementId = createStatement(alice, pensionId, "2026-01-01", "100000");
        String oiId = createOtherIncome(alice, "Rent");

        mockMvc.perform(get("/api/v1/audit/pensions/" + pensionId).cookie(bob))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/audit/statements/" + statementId).cookie(bob))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/audit/other-income/" + oiId).cookie(bob))
                .andExpect(status().isNotFound());
    }

    @Test
    void auditCapturesTagsForPensionAndOtherIncome() throws Exception {
        Cookie cookie = register("audit_tags_carol", "password123");
        String tagIsa = createTag(cookie, "ISA");
        String tagEmployer = createTag(cookie, "Employer");

        String pensionId = createPension(cookie, "P-tags", "[" + tagIsa + "," + tagEmployer + "]");
        String oiId = createOtherIncome(cookie, "Rent-tags", "[" + tagIsa + "]");

        mockMvc.perform(get("/api/v1/audit/pensions/" + pensionId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tags").value("Employer, ISA"));

        mockMvc.perform(get("/api/v1/audit/other-income/" + oiId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tags").value("ISA"));
    }

    private String createPension(Cookie cookie, String name) throws Exception {
        return createPension(cookie, name, "[]");
    }

    private String createPension(Cookie cookie, String name, String tagIdsJson) throws Exception {
        return jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\",\"tagIds\":" + tagIdsJson + "}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.pensionId");
    }

    private String createStatement(Cookie cookie, String pensionId, String date, String planValue) throws Exception {
        return jsonStringAt(mockMvc.perform(post("/api/v1/pensions/" + pensionId + "/statements")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"" + date + "\",\"planValue\":" + planValue
                                + ",\"projectedAnnualAmount\":5000}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.statementId");
    }

    private String createOtherIncome(Cookie cookie, String name) throws Exception {
        return createOtherIncome(cookie, name, "[]");
    }

    private String createOtherIncome(Cookie cookie, String name, String tagIdsJson) throws Exception {
        return jsonStringAt(mockMvc.perform(post("/api/v1/other-income")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"annualAmount\":60000,\"tagIds\":" + tagIdsJson + "}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.id");
    }

    private String createTag(Cookie cookie, String name) throws Exception {
        return jsonStringAt(mockMvc.perform(post("/api/v1/tags")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.id");
    }

    private String createStatePension(Cookie cookie, String name) throws Exception {
        return jsonStringAt(mockMvc.perform(post("/api/v1/state-pension")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"yearlyAmount\":11000,\"takesEffectYear\":2040}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.id");
    }

    private Cookie register(String username, String password) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        return new Cookie("pp_jwt", cookieValueFrom(response, "pp_jwt"));
    }

    private static String jsonStringAt(org.springframework.test.web.servlet.MvcResult result, String pointer) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), pointer).toString();
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
