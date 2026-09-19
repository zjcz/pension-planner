package dev.jonclarke.pensionplanner.pension;

import dev.jonclarke.pensionplanner.audit.PensionAudit;
import dev.jonclarke.pensionplanner.audit.PensionAuditRepository;
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
class PensionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PensionRepository pensionRepository;

    @Autowired
    PensionStatementRepository statementRepository;

    @Autowired
    PensionAuditRepository pensionAuditRepository;

    @Autowired
    PensionStatementAuditRepository statementAuditRepository;

    @Test
    void createListGetUpdateDeleteFlow() throws Exception {
        Cookie cookie = register("alice", "password123");

        String createBody = "{\"name\":\"Nest Egg\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\","
                + "\"notes\":\"main pot\",\"color\":\"#336699\"}";

        String createdId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nest Egg"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.color").value("#336699"))
                .andExpect(jsonPath("$.statusDate").isNotEmpty())
                .andReturn(), "$.pensionId");

        mockMvc.perform(get("/api/v1/pensions").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Nest Egg"));

        mockMvc.perform(get("/api/v1/pensions/" + createdId).cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pensionId").value(createdId));

        mockMvc.perform(put("/api/v1/pensions/" + createdId)
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Nest Egg (renamed)\",\"maturityDate\":\"2046-01-01\","
                                + "\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nest Egg (renamed)"))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.statusDate").isNotEmpty());

        mockMvc.perform(delete("/api/v1/pensions/" + createdId).cookie(cookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pensions/" + createdId).cookie(cookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationErrorsReturnBadRequest() throws Exception {
        Cookie cookie = register("bob", "password123");

        mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name")));

        mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"P\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\","
                                + "\"color\":\"blue\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("color")));

        mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"P\",\"maturityDate\":\"2045-08-01\",\"status\":\"SOMETHING_ELSE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pensionsAreIsolatedPerUser() throws Exception {
        Cookie alice = register("alice2", "password123");
        Cookie bob = register("bob2", "password123");

        String createdId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(alice)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Alice Pension\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.pensionId");

        mockMvc.perform(get("/api/v1/pensions").cookie(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/pensions/" + createdId).cookie(bob))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/pensions/" + createdId)
                        .cookie(bob)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Hacked\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/pensions/" + createdId).cookie(bob))
                .andExpect(status().isNotFound());
    }

    @Test
    void auditRowsWrittenForCreateUpdateDelete() throws Exception {
        Cookie cookie = register("carol", "password123");

        String createdId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Audited\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.pensionId");

        assertThat(pensionActionsFor(Long.parseLong(createdId))).containsExactly("CREATE");

        mockMvc.perform(put("/api/v1/pensions/" + createdId)
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Audited v2\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());

        assertThat(pensionActionsFor(Long.parseLong(createdId))).containsExactly("CREATE", "UPDATE");

        mockMvc.perform(delete("/api/v1/pensions/" + createdId).cookie(cookie))
                .andExpect(status().isNoContent());

        assertThat(pensionActionsFor(Long.parseLong(createdId))).containsExactly("CREATE", "UPDATE", "DELETE");
    }

    @Test
    void deletingPensionCascadesAndAuditsStatements() throws Exception {
        Cookie cookie = register("dave", "password123");

        String createdId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"With Statements\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.pensionId");

        Pension pension = pensionRepository.findById(Long.parseLong(createdId)).orElseThrow();
        PensionStatement statement = new PensionStatement();
        statement.setPensionId(pension.getPensionId());
        statement.setStatementDate(java.time.LocalDate.of(2026, 1, 1));
        statement.setPlanValue(100000L);
        statement.setProjectedAnnualAmount(5000L);
        statementRepository.save(statement);

        mockMvc.perform(delete("/api/v1/pensions/" + createdId).cookie(cookie))
                .andExpect(status().isNoContent());

        assertThat(statementRepository.findByPensionIdOrderByStatementDateAsc(pension.getPensionId())).isEmpty();
        assertThat(statementAuditRepository.findAll().stream()
                .filter(audit -> pension.getPensionId().equals(audit.getPensionId()))
                .map(PensionStatementAudit::getAction)
                .toList()).containsExactly("DELETE");
        assertThat(pensionActionsFor(pension.getPensionId())).containsExactly("CREATE", "DELETE");
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

    private List<String> pensionActionsFor(Long pensionId) {
        return pensionAuditRepository.findAll().stream()
                .filter(audit -> audit.getPensionId() != null && audit.getPensionId().equals(pensionId))
                .map(PensionAudit::getAction)
                .toList();
    }
}
