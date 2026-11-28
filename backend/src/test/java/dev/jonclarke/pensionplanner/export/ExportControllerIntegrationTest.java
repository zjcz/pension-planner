package dev.jonclarke.pensionplanner.export;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:exportflow?mode=memory&cache=shared")
@AutoConfigureMockMvc
class ExportControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void exportPensionsAndStatementsZip() throws Exception {
        Cookie cookie = register("exporter1", "password123");

        String pensionId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"My SIPP\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.pensionId");

        mockMvc.perform(post("/api/v1/pensions/" + pensionId + "/statements")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"statementDate\":\"2026-01-01\",\"planValue\":80000,\"projectedAnnualAmount\":4000}"))
                .andExpect(status().isCreated());

        byte[] zipBytes = mockMvc.perform(get("/api/v1/export")
                        .cookie(cookie)
                        .param("format", "CSV")
                        .param("type", "PENSIONS")
                        .param("includeStatements", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.matchesPattern(
                                "attachment; filename=\"pension-planner-pensions-export-\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.zip\"")))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Map<String, String> entries = ExportServiceTest.unzip(zipBytes);
        assertThat(entries).containsOnlyKeys("pensions.csv", "statements.csv");
        assertThat(entries.get("pensions.csv")).contains("My SIPP");
        assertThat(entries.get("statements.csv")).contains("80000");
    }

    @Test
    void exportRejectsCrossUserPensionIdForStatements() throws Exception {
        Cookie alice = register("exporter_alice", "password123");
        Cookie bob = register("exporter_bob", "password123");

        String alicePensionId = jsonStringAt(mockMvc.perform(post("/api/v1/pensions")
                        .cookie(alice)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Alice Pot\",\"maturityDate\":\"2045-08-01\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isCreated())
                .andReturn(), "$.pensionId");

        mockMvc.perform(get("/api/v1/export")
                        .cookie(bob)
                        .param("format", "JSON")
                        .param("type", "STATEMENTS")
                        .param("pensionId", alicePensionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void exportRequiresAuthenticationAndValidParams() throws Exception {
        mockMvc.perform(get("/api/v1/export")
                        .param("format", "CSV")
                        .param("type", "PENSIONS"))
                .andExpect(status().isUnauthorized());

        Cookie cookie = register("exporter_valid", "password123");
        mockMvc.perform(get("/api/v1/export")
                        .cookie(cookie)
                        .param("format", "INVALID")
                        .param("type", "PENSIONS"))
                .andExpect(status().isBadRequest());
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
}
