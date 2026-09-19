package dev.jonclarke.pensionplanner.pension;

import dev.jonclarke.pensionplanner.audit.PensionStatementAudit;
import dev.jonclarke.pensionplanner.audit.PensionStatementAuditRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:pensionflow?mode=memory&cache=shared")
@AutoConfigureMockMvc
class StatementControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PensionRepository pensionRepository;

    @Autowired
    PensionStatementRepository statementRepository;

    @Autowired
    PensionStatementAuditRepository statementAuditRepository;

    @Test
    void createListGetUpdateDeleteFlow() throws Exception {
        Cookie cookie = register("stmt_alice", "password123");
        String pensionId = createPension(cookie, "Pension 1");

        String createBody = "{\"statementDate\":\"2026-01-01\",\"planValue\":100000,"
                + "\"projectedAnnualAmount\":5000,\"yearlyCharges\":500,\"statementNotes\":\"First\"}";

        String statementId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions/" + pensionId + "/statements")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planValue").value(100000))
                .andExpect(jsonPath("$.yearlyCharges").value(500))
                .andExpect(jsonPath("$.statementNotes").value("First"))
                .andReturn(), "$.statementId");

        mockMvc.perform(get("/api/v1/pensions/" + pensionId + "/statements").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].statementNotes").value("First"));

        mockMvc.perform(get("/api/v1/pensions/" + pensionId + "/statements/" + statementId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(statementId));

        mockMvc.perform(put("/api/v1/pensions/" + pensionId + "/statements/" + statementId)
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"2026-06-01\",\"planValue\":150000,"
                                + "\"projectedAnnualAmount\":7500,\"statementNotes\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planValue").value(150000))
                .andExpect(jsonPath("$.statementNotes").value("Updated"));

        mockMvc.perform(delete("/api/v1/pensions/" + pensionId + "/statements/" + statementId).cookie(cookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pensions/" + pensionId + "/statements/" + statementId).cookie(cookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationErrorsReturnBadRequest() throws Exception {
        Cookie cookie = register("stmt_bob", "password123");
        String pensionId = createPension(cookie, "Pension 2");

        mockMvc.perform(post("/api/v1/pensions/" + pensionId + "/statements")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"planValue\":100000,\"projectedAnnualAmount\":5000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("statementDate")));

        mockMvc.perform(post("/api/v1/pensions/" + pensionId + "/statements")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"2026-01-01\",\"projectedAnnualAmount\":5000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("planValue")));
    }

    @Test
    void statementsAreIsolatedPerPension() throws Exception {
        Cookie cookie = register("stmt_carol", "password123");
        String pensionId1 = createPension(cookie, "Pension A");
        String pensionId2 = createPension(cookie, "Pension B");

        String statementId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions/" + pensionId1 + "/statements")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"2026-01-01\",\"planValue\":100000,"
                                + "\"projectedAnnualAmount\":5000}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.statementId");

        mockMvc.perform(get("/api/v1/pensions/" + pensionId2 + "/statements").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/pensions/" + pensionId2 + "/statements/" + statementId).cookie(cookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void auditRowsWrittenForCreateUpdateDelete() throws Exception {
        Cookie cookie = register("stmt_dave", "password123");
        String pensionId = createPension(cookie, "Pension 3");

        String statementId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions/" + pensionId + "/statements")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"2026-01-01\",\"planValue\":100000,"
                                + "\"projectedAnnualAmount\":5000}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.statementId");

        assertThat(statementActionsFor(Long.parseLong(statementId))).containsExactly("CREATE");

        mockMvc.perform(put("/api/v1/pensions/" + pensionId + "/statements/" + statementId)
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"2026-06-01\",\"planValue\":150000,"
                                + "\"projectedAnnualAmount\":7500}"))
                .andExpect(status().isOk());

        assertThat(statementActionsFor(Long.parseLong(statementId))).containsExactly("CREATE", "UPDATE");

        mockMvc.perform(delete("/api/v1/pensions/" + pensionId + "/statements/" + statementId).cookie(cookie))
                .andExpect(status().isNoContent());

        assertThat(statementActionsFor(Long.parseLong(statementId))).containsExactly("CREATE", "UPDATE", "DELETE");
    }

    private String createPension(Cookie cookie, String name) throws Exception {
        return jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.pensionId");
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
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), pointer).toString();
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

    private List<String> statementActionsFor(Long statementId) {
        return statementAuditRepository.findAll().stream()
                .filter(audit -> audit.getStatementId() != null && audit.getStatementId().equals(statementId))
                .map(PensionStatementAudit::getAction)
                .toList();
    }
}
