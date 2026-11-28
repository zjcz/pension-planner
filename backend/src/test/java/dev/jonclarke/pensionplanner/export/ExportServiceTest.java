package dev.jonclarke.pensionplanner.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jonclarke.pensionplanner.income.OtherIncome;
import dev.jonclarke.pensionplanner.income.OtherIncomeService;
import dev.jonclarke.pensionplanner.income.StatePension;
import dev.jonclarke.pensionplanner.income.StatePensionService;
import dev.jonclarke.pensionplanner.pension.Pension;
import dev.jonclarke.pensionplanner.pension.PensionService;
import dev.jonclarke.pensionplanner.pension.PensionStatement;
import dev.jonclarke.pensionplanner.pension.PensionStatus;
import dev.jonclarke.pensionplanner.pension.StatementService;
import dev.jonclarke.pensionplanner.tag.TagDto;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExportServiceTest {

    private final PensionService pensionService = mock(PensionService.class);
    private final StatementService statementService = mock(StatementService.class);
    private final OtherIncomeService otherIncomeService = mock(OtherIncomeService.class);
    private final StatePensionService statePensionService = mock(StatePensionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExportService exportService = new ExportService(
            pensionService, statementService, otherIncomeService, statePensionService, objectMapper);

    @Test
    void exportPensionsCsvWithStatementsIncluded() throws IOException {
        Pension p = samplePension(1L, "Aviva, Workplace");
        PensionStatement s = sampleStatement(10L, 1L);

        when(pensionService.listForUser(42L)).thenReturn(List.of(p));
        when(pensionService.getTagsForPensionsByPensionId(List.of(1L)))
                .thenReturn(Map.of(1L, List.of(new TagDto(2L, "Workplace"), new TagDto(1L, "Core"))));
        when(statementService.listForPension(42L, 1L)).thenReturn(List.of(s));

        byte[] zipBytes = exportService.exportData(42L, ExportFormat.CSV, ExportType.PENSIONS, true, null);
        Map<String, String> entries = unzip(zipBytes);

        assertThat(entries).containsOnlyKeys("pensions.csv", "statements.csv");
        assertThat(entries.get("pensions.csv"))
                .contains("pensionId,name,maturityDate,status,statusDate,color,providerName,policyNumber,workplaceName,tags,notes")
                .contains("1,\"Aviva, Workplace\",2045-06-01,ACTIVE,2026-01-15,#36A2EB,Aviva,POL-1,Acme,\"Core, Workplace\",Main pot");
        assertThat(entries.get("statements.csv"))
                .contains("statementId,pensionId,pensionName,statementDate,planValue,projectedAnnualAmount,yearlyCharges,transferValue,amountPaidIn,statementNotes")
                .contains("10,1,\"Aviva, Workplace\",2026-01-01,50000,2500,150,49000,40000,Annual note");
    }

    @Test
    void exportPensionsJsonWithoutStatements() throws IOException {
        Pension p = samplePension(1L, "SIPP");

        when(pensionService.listForUser(42L)).thenReturn(List.of(p));
        when(pensionService.getTagsForPensionsByPensionId(List.of(1L))).thenReturn(Map.of());

        byte[] zipBytes = exportService.exportData(42L, ExportFormat.JSON, ExportType.PENSIONS, false, null);
        Map<String, String> entries = unzip(zipBytes);

        assertThat(entries).containsOnlyKeys("pensions.json");
        assertThat(entries.get("pensions.json"))
                .contains("\"pensionId\" : 1")
                .contains("\"name\" : \"SIPP\"")
                .contains("\"maturityDate\" : \"2045-06-01\"");
        verify(statementService, never()).listForPension(42L, 1L);
    }

    @Test
    void exportStatementsXmlForSpecificPension() throws IOException {
        Pension p = samplePension(5L, "Nest & Co");
        PensionStatement s = sampleStatement(20L, 5L);

        when(pensionService.getForUser(42L, 5L)).thenReturn(p);
        when(statementService.listForPension(42L, 5L)).thenReturn(List.of(s));

        byte[] zipBytes = exportService.exportData(42L, ExportFormat.XML, ExportType.STATEMENTS, false, 5L);
        Map<String, String> entries = unzip(zipBytes);

        assertThat(entries).containsOnlyKeys("statements.xml");
        assertThat(entries.get("statements.xml"))
                .contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .contains("<statements>")
                .contains("<statementId>20</statementId>")
                .contains("<pensionName>Nest &amp; Co</pensionName>")
                .contains("<planValue>50000</planValue>");
        verify(pensionService, never()).listForUser(42L);
    }

    @Test
    void exportStatementsForAllPensionsWhenPensionIdIsNull() throws IOException {
        Pension p1 = samplePension(1L, "Pension A");
        Pension p2 = samplePension(2L, "Pension B");
        PensionStatement s1 = sampleStatement(10L, 1L);
        PensionStatement s2 = sampleStatement(20L, 2L);

        when(pensionService.listForUser(42L)).thenReturn(List.of(p1, p2));
        when(statementService.listForPension(42L, 1L)).thenReturn(List.of(s1));
        when(statementService.listForPension(42L, 2L)).thenReturn(List.of(s2));

        byte[] zipBytes = exportService.exportData(42L, ExportFormat.JSON, ExportType.STATEMENTS, false, null);
        Map<String, String> entries = unzip(zipBytes);

        assertThat(entries).containsOnlyKeys("statements.json");
        assertThat(entries.get("statements.json"))
                .contains("\"pensionName\" : \"Pension A\"")
                .contains("\"pensionName\" : \"Pension B\"");
    }

    @Test
    void exportOtherIncomesInAllFormats() throws IOException {
        OtherIncome oi = new OtherIncome();
        oi.setId(3L);
        oi.setUserId(42L);
        oi.setName("Rental");
        oi.setAnnualAmount(12000L);
        oi.setNotes("Flat 1");

        when(otherIncomeService.listForUser(42L)).thenReturn(List.of(oi));
        when(otherIncomeService.getTagsForOtherIncome(3L)).thenReturn(List.of(new TagDto(1L, "Property")));

        Map<String, String> csvEntries = unzip(exportService.exportData(42L, ExportFormat.CSV, ExportType.OTHER_INCOMES, false, null));
        assertThat(csvEntries).containsOnlyKeys("other-incomes.csv");
        assertThat(csvEntries.get("other-incomes.csv")).contains("3,Rental,12000,Property,Flat 1");

        Map<String, String> xmlEntries = unzip(exportService.exportData(42L, ExportFormat.XML, ExportType.OTHER_INCOMES, false, null));
        assertThat(xmlEntries).containsOnlyKeys("other-incomes.xml");
        assertThat(xmlEntries.get("other-incomes.xml")).contains("<otherIncome>").contains("<tag>Property</tag>");

        Map<String, String> jsonEntries = unzip(exportService.exportData(42L, ExportFormat.JSON, ExportType.OTHER_INCOMES, false, null));
        assertThat(jsonEntries).containsOnlyKeys("other-incomes.json");
        assertThat(jsonEntries.get("other-incomes.json")).contains("\"annualAmount\" : 12000");
    }

    @Test
    void exportStatePensionInAllFormats() throws IOException {
        StatePension sp = new StatePension();
        sp.setId(7L);
        sp.setUserId(42L);
        sp.setName("UK State Pension");
        sp.setYearlyAmount(11500L);
        sp.setTakesEffectYear(2045);
        sp.setNotes("Forecast");

        when(statePensionService.listForUser(42L)).thenReturn(List.of(sp));

        Map<String, String> csvEntries = unzip(exportService.exportData(42L, ExportFormat.CSV, ExportType.STATE_PENSION, false, null));
        assertThat(csvEntries).containsOnlyKeys("state-pension.csv");
        assertThat(csvEntries.get("state-pension.csv")).contains("7,UK State Pension,11500,2045,Forecast");

        Map<String, String> xmlEntries = unzip(exportService.exportData(42L, ExportFormat.XML, ExportType.STATE_PENSION, false, null));
        assertThat(xmlEntries).containsOnlyKeys("state-pension.xml");
        assertThat(xmlEntries.get("state-pension.xml")).contains("<statePension>").contains("<takesEffectYear>2045</takesEffectYear>");

        Map<String, String> jsonEntries = unzip(exportService.exportData(42L, ExportFormat.JSON, ExportType.STATE_PENSION, false, null));
        assertThat(jsonEntries).containsOnlyKeys("state-pension.json");
        assertThat(jsonEntries.get("state-pension.json")).contains("\"yearlyAmount\" : 11500");
    }

    @Test
    void exportAllIncludesAllFourDataTypesInZip() throws IOException {
        Pension p = samplePension(1L, "Workplace Pension");
        PensionStatement s = sampleStatement(10L, 1L);
        OtherIncome oi = new OtherIncome();
        oi.setId(3L);
        oi.setUserId(42L);
        oi.setName("Rental");
        oi.setAnnualAmount(12000L);
        StatePension sp = new StatePension();
        sp.setId(7L);
        sp.setUserId(42L);
        sp.setName("UK State Pension");
        sp.setYearlyAmount(11500L);
        sp.setTakesEffectYear(2045);

        when(pensionService.listForUser(42L)).thenReturn(List.of(p));
        when(pensionService.getTagsForPensionsByPensionId(List.of(1L))).thenReturn(Map.of());
        when(statementService.listForPension(42L, 1L)).thenReturn(List.of(s));
        when(otherIncomeService.listForUser(42L)).thenReturn(List.of(oi));
        when(otherIncomeService.getTagsForOtherIncome(3L)).thenReturn(List.of());
        when(statePensionService.listForUser(42L)).thenReturn(List.of(sp));

        Map<String, String> entries = unzip(exportService.exportData(42L, ExportFormat.CSV, ExportType.ALL, true, null));

        assertThat(entries).containsOnlyKeys("pensions.csv", "statements.csv", "other-incomes.csv", "state-pension.csv");
        assertThat(entries.get("pensions.csv")).contains("Workplace Pension");
        assertThat(entries.get("statements.csv")).contains("50000");
        assertThat(entries.get("other-incomes.csv")).contains("Rental");
        assertThat(entries.get("state-pension.csv")).contains("UK State Pension");
    }

    private static Pension samplePension(Long id, String name) {
        Pension p = new Pension();
        p.setPensionId(id);
        p.setUserId(42L);
        p.setName(name);
        p.setMaturityDate(LocalDate.of(2045, 6, 1));
        p.setStatus(PensionStatus.ACTIVE);
        p.setStatusDate(LocalDate.of(2026, 1, 15));
        p.setColor("#36A2EB");
        p.setProviderName("Aviva");
        p.setPolicyNumber("POL-1");
        p.setWorkplaceName("Acme");
        p.setNotes("Main pot");
        return p;
    }

    private static PensionStatement sampleStatement(Long statementId, Long pensionId) {
        PensionStatement s = new PensionStatement();
        s.setStatementId(statementId);
        s.setPensionId(pensionId);
        s.setStatementDate(LocalDate.of(2026, 1, 1));
        s.setPlanValue(50000L);
        s.setProjectedAnnualAmount(2500L);
        s.setYearlyCharges(150L);
        s.setTransferValue(49000L);
        s.setAmountPaidIn(40000L);
        s.setStatementNotes("Annual note");
        return s;
    }

    static Map<String, String> unzip(byte[] zipBytes) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                result.put(entry.getName(), new String(zis.readAllBytes(), StandardCharsets.UTF_8));
                zis.closeEntry();
            }
        }
        return result;
    }
}
