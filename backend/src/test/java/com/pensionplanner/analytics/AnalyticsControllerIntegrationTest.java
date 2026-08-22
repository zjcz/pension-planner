package com.pensionplanner.analytics;

import com.pensionplanner.pension.Pension;
import com.pensionplanner.pension.PensionRepository;
import com.pensionplanner.pension.PensionStatement;
import com.pensionplanner.pension.PensionStatementRepository;
import com.pensionplanner.pension.PensionStatus;
import com.pensionplanner.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:pensionflow?mode=memory&cache=shared")
@AutoConfigureMockMvc
class AnalyticsControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PensionRepository pensionRepository;

    @Autowired
    PensionStatementRepository statementRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void emptyAnalyticsReturnsValidStructure() throws Exception {
        Cookie cookie = register("an_alice", "password123");

        mockMvc.perform(get("/api/v1/analytics").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.today").exists())
                .andExpect(jsonPath("$.projections").isArray())
                .andExpect(jsonPath("$.pensionGrowthCosts").isArray())
                .andExpect(jsonPath("$.pensionIncomeBreakdown").isArray())
                .andExpect(jsonPath("$.pensionHistorySeries").isArray());
    }

    @Test
    void analyticsIncludesPensionData() throws Exception {
        Cookie cookie = register("an_bob", "password123");
        Long userId = getUserId("an_bob");

        Pension pension = new Pension();
        pension.setUserId(userId);
        pension.setName("Test Pension");
        pension.setMaturityDate(LocalDate.of(2045, 1, 1));
        pension.setStatus(PensionStatus.ACTIVE);
        pensionRepository.save(pension);

        PensionStatement stmt = new PensionStatement();
        stmt.setPensionId(pension.getPensionId());
        stmt.setStatementDate(LocalDate.of(2026, 1, 1));
        stmt.setPlanValue(200000L);
        stmt.setProjectedAnnualAmount(10000L);
        stmt.setAmountPaidIn(150000L);
        stmt.setYearlyCharges(500L);
        statementRepository.save(stmt);

        mockMvc.perform(get("/api/v1/analytics").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pensionGrowthCosts.length()").value(1))
                .andExpect(jsonPath("$.pensionGrowthCosts[0].name").value("Test Pension"))
                .andExpect(jsonPath("$.pensionGrowthCosts[0].growthValue").value(50000))
                .andExpect(jsonPath("$.pensionGrowthCosts[0].cumulativeCharges").value(500))
                .andExpect(jsonPath("$.pensionIncomeBreakdown.length()").value(1))
                .andExpect(jsonPath("$.pensionHistorySeries.length()").value(2))
                .andExpect(jsonPath("$.pensionHistorySeries[1].name").value("Total"));
    }

    private Cookie register(String username, String password) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/auth/register")
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
