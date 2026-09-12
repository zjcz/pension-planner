package com.pensionplanner.dashboard;

import com.pensionplanner.income.OtherIncome;
import com.pensionplanner.income.OtherIncomeRepository;
import com.pensionplanner.income.StatePension;
import com.pensionplanner.income.StatePensionRepository;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:pensionflow?mode=memory&cache=shared")
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PensionRepository pensionRepository;

    @Autowired
    PensionStatementRepository statementRepository;

    @Autowired
    StatePensionRepository statePensionRepository;

    @Autowired
    OtherIncomeRepository otherIncomeRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void emptyDashboardReturnsZeroes() throws Exception {
        Cookie cookie = register("dash_alice", "password123");

        mockMvc.perform(get("/api/v1/dashboard").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPortfolioValue").value(0))
                .andExpect(jsonPath("$.totalProjectedAnnualIncome").value(0))
                .andExpect(jsonPath("$.otherIncome").isEmpty());
    }

    @Test
    void dashboardAggregatesPensionData() throws Exception {
        Cookie cookie = register("dash_bob", "password123");

        Pension pension = new Pension();
        pension.setUserId(getUserId("dash_bob"));
        pension.setName("Test Pension");
        pension.setMaturityDate(LocalDate.of(2045, 1, 1));
        pension.setStatus(PensionStatus.ACTIVE);
        pensionRepository.save(pension);

        PensionStatement stmt = new PensionStatement();
        stmt.setPensionId(pension.getPensionId());
        stmt.setStatementDate(LocalDate.of(2026, 1, 1));
        stmt.setPlanValue(200000L);
        stmt.setProjectedAnnualAmount(10000L);
        statementRepository.save(stmt);

        mockMvc.perform(get("/api/v1/dashboard").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPortfolioValue").value(200000))
                .andExpect(jsonPath("$.totalProjectedAnnualIncome").value(10000));
    }

    @Test
    void dashboardIncludesAllIncomeSources() throws Exception {
        Cookie cookie = register("dash_carol", "password123");
        Long userId = getUserId("dash_carol");

        StatePension sp1 = new StatePension();
        sp1.setUserId(userId);
        sp1.setName("Mine");
        sp1.setYearlyAmount(11000L);
        sp1.setTakesEffectYear(2028);
        statePensionRepository.save(sp1);

        StatePension sp2 = new StatePension();
        sp2.setUserId(userId);
        sp2.setName("Partner");
        sp2.setYearlyAmount(6000L);
        sp2.setTakesEffectYear(2030);
        statePensionRepository.save(sp2);

        OtherIncome oi = new OtherIncome();
        oi.setUserId(userId);
        oi.setName("Freelance");
        oi.setAnnualAmount(3000L);
        otherIncomeRepository.save(oi);

        mockMvc.perform(get("/api/v1/dashboard").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjectedAnnualIncome").value(20000))
                .andExpect(jsonPath("$.statePensions.length()").value(2))
                .andExpect(jsonPath("$.statePensions[0].name").value("Mine"))
                .andExpect(jsonPath("$.statePensions[1].yearlyAmount").value(6000))
                .andExpect(jsonPath("$.otherIncome.length()").value(1))
                .andExpect(jsonPath("$.otherIncome[0].name").value("Freelance"));
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
