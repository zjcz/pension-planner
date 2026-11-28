package dev.jonclarke.pensionplanner.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jonclarke.pensionplanner.income.OtherIncome;
import dev.jonclarke.pensionplanner.income.OtherIncomeService;
import dev.jonclarke.pensionplanner.income.StatePension;
import dev.jonclarke.pensionplanner.income.StatePensionService;
import dev.jonclarke.pensionplanner.pension.Pension;
import dev.jonclarke.pensionplanner.pension.PensionService;
import dev.jonclarke.pensionplanner.pension.PensionStatement;
import dev.jonclarke.pensionplanner.pension.StatementService;
import dev.jonclarke.pensionplanner.tag.TagDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExportService {

    private final PensionService pensionService;
    private final StatementService statementService;
    private final OtherIncomeService otherIncomeService;
    private final StatePensionService statePensionService;
    private final ObjectMapper objectMapper;

    public ExportService(PensionService pensionService,
                         StatementService statementService,
                         OtherIncomeService otherIncomeService,
                         StatePensionService statePensionService,
                         ObjectMapper objectMapper) {
        this.pensionService = pensionService;
        this.statementService = statementService;
        this.otherIncomeService = otherIncomeService;
        this.statePensionService = statePensionService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public byte[] exportData(Long userId, ExportFormat format, ExportType type,
                             boolean includeStatements, Long pensionId) {
        Map<String, String> files = new LinkedHashMap<>();
        String ext = format.getExtension();

        switch (type) {
            case ALL -> {
                List<Pension> pensions = pensionService.listForUser(userId);
                List<Long> pensionIds = pensions.stream().map(Pension::getPensionId).toList();
                Map<Long, List<TagDto>> tagsByPension = pensionService.getTagsForPensionsByPensionId(pensionIds);
                files.put("pensions." + ext, formatPensions(pensions, tagsByPension, format));

                List<StatementRow> statements = loadStatementsForPensions(userId, pensions);
                files.put("statements." + ext, formatStatements(statements, format));

                List<OtherIncome> otherIncomes = otherIncomeService.listForUser(userId);
                files.put("other-incomes." + ext, formatOtherIncomes(otherIncomes, format));

                List<StatePension> statePensions = statePensionService.listForUser(userId);
                files.put("state-pension." + ext, formatStatePensions(statePensions, format));
            }
            case PENSIONS -> {
                List<Pension> pensions = pensionService.listForUser(userId);
                List<Long> pensionIds = pensions.stream().map(Pension::getPensionId).toList();
                Map<Long, List<TagDto>> tagsByPension = pensionService.getTagsForPensionsByPensionId(pensionIds);
                files.put("pensions." + ext, formatPensions(pensions, tagsByPension, format));

                if (includeStatements) {
                    List<StatementRow> statements = loadStatementsForPensions(userId, pensions);
                    files.put("statements." + ext, formatStatements(statements, format));
                }
            }
            case STATEMENTS -> {
                List<Pension> targetPensions;
                if (pensionId != null) {
                    targetPensions = List.of(pensionService.getForUser(userId, pensionId));
                } else {
                    targetPensions = pensionService.listForUser(userId);
                }
                List<StatementRow> statements = loadStatementsForPensions(userId, targetPensions);
                files.put("statements." + ext, formatStatements(statements, format));
            }
            case OTHER_INCOMES -> {
                List<OtherIncome> otherIncomes = otherIncomeService.listForUser(userId);
                files.put("other-incomes." + ext, formatOtherIncomes(otherIncomes, format));
            }
            case STATE_PENSION -> {
                List<StatePension> statePensions = statePensionService.listForUser(userId);
                files.put("state-pension." + ext, formatStatePensions(statePensions, format));
            }
        }

        return buildZip(files);
    }

    private record StatementRow(Pension pension, PensionStatement statement) {}

    private List<StatementRow> loadStatementsForPensions(Long userId, List<Pension> pensions) {
        List<StatementRow> rows = new ArrayList<>();
        for (Pension pension : pensions) {
            List<PensionStatement> statements = statementService.listForPension(userId, pension.getPensionId());
            for (PensionStatement statement : statements) {
                rows.add(new StatementRow(pension, statement));
            }
        }
        return rows;
    }

    private String formatPensions(List<Pension> pensions, Map<Long, List<TagDto>> tagsByPension, ExportFormat format) {
        return switch (format) {
            case CSV -> {
                StringBuilder sb = new StringBuilder();
                sb.append("pensionId,name,maturityDate,status,statusDate,color,providerName,policyNumber,workplaceName,tags,notes\n");
                for (Pension p : pensions) {
                    List<String> tagNames = extractTagNames(tagsByPension.getOrDefault(p.getPensionId(), List.of()));
                    sb.append(csvRow(
                            p.getPensionId(),
                            p.getName(),
                            p.getMaturityDate(),
                            p.getStatus(),
                            p.getStatusDate(),
                            p.getColor(),
                            p.getProviderName(),
                            p.getPolicyNumber(),
                            p.getWorkplaceName(),
                            String.join(", ", tagNames),
                            p.getNotes()
                    ));
                }
                yield sb.toString();
            }
            case JSON -> {
                List<Map<String, Object>> list = new ArrayList<>();
                for (Pension p : pensions) {
                    List<String> tagNames = extractTagNames(tagsByPension.getOrDefault(p.getPensionId(), List.of()));
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("pensionId", p.getPensionId());
                    item.put("name", p.getName());
                    item.put("maturityDate", p.getMaturityDate() != null ? p.getMaturityDate().toString() : null);
                    item.put("status", p.getStatus() != null ? p.getStatus().name() : null);
                    item.put("statusDate", p.getStatusDate() != null ? p.getStatusDate().toString() : null);
                    item.put("color", p.getColor());
                    item.put("providerName", p.getProviderName());
                    item.put("policyNumber", p.getPolicyNumber());
                    item.put("workplaceName", p.getWorkplaceName());
                    item.put("tags", tagNames);
                    item.put("notes", p.getNotes());
                    list.add(item);
                }
                yield toJson(list);
            }
            case XML -> {
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<pensions>\n");
                for (Pension p : pensions) {
                    List<String> tagNames = extractTagNames(tagsByPension.getOrDefault(p.getPensionId(), List.of()));
                    sb.append("  <pension>\n");
                    appendXmlTag(sb, 4, "pensionId", p.getPensionId());
                    appendXmlTag(sb, 4, "name", p.getName());
                    appendXmlTag(sb, 4, "maturityDate", p.getMaturityDate());
                    appendXmlTag(sb, 4, "status", p.getStatus());
                    appendXmlTag(sb, 4, "statusDate", p.getStatusDate());
                    appendXmlTag(sb, 4, "color", p.getColor());
                    appendXmlTag(sb, 4, "providerName", p.getProviderName());
                    appendXmlTag(sb, 4, "policyNumber", p.getPolicyNumber());
                    appendXmlTag(sb, 4, "workplaceName", p.getWorkplaceName());
                    appendXmlTagsList(sb, 4, tagNames);
                    appendXmlTag(sb, 4, "notes", p.getNotes());
                    sb.append("  </pension>\n");
                }
                sb.append("</pensions>\n");
                yield sb.toString();
            }
        };
    }

    private String formatStatements(List<StatementRow> rows, ExportFormat format) {
        return switch (format) {
            case CSV -> {
                StringBuilder sb = new StringBuilder();
                sb.append("statementId,pensionId,pensionName,statementDate,planValue,projectedAnnualAmount,yearlyCharges,transferValue,amountPaidIn,statementNotes\n");
                for (StatementRow row : rows) {
                    PensionStatement s = row.statement();
                    sb.append(csvRow(
                            s.getStatementId(),
                            s.getPensionId(),
                            row.pension().getName(),
                            s.getStatementDate(),
                            s.getPlanValue(),
                            s.getProjectedAnnualAmount(),
                            s.getYearlyCharges(),
                            s.getTransferValue(),
                            s.getAmountPaidIn(),
                            s.getStatementNotes()
                    ));
                }
                yield sb.toString();
            }
            case JSON -> {
                List<Map<String, Object>> list = new ArrayList<>();
                for (StatementRow row : rows) {
                    PensionStatement s = row.statement();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("statementId", s.getStatementId());
                    item.put("pensionId", s.getPensionId());
                    item.put("pensionName", row.pension().getName());
                    item.put("statementDate", s.getStatementDate() != null ? s.getStatementDate().toString() : null);
                    item.put("planValue", s.getPlanValue());
                    item.put("projectedAnnualAmount", s.getProjectedAnnualAmount());
                    item.put("yearlyCharges", s.getYearlyCharges());
                    item.put("transferValue", s.getTransferValue());
                    item.put("amountPaidIn", s.getAmountPaidIn());
                    item.put("statementNotes", s.getStatementNotes());
                    list.add(item);
                }
                yield toJson(list);
            }
            case XML -> {
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<statements>\n");
                for (StatementRow row : rows) {
                    PensionStatement s = row.statement();
                    sb.append("  <statement>\n");
                    appendXmlTag(sb, 4, "statementId", s.getStatementId());
                    appendXmlTag(sb, 4, "pensionId", s.getPensionId());
                    appendXmlTag(sb, 4, "pensionName", row.pension().getName());
                    appendXmlTag(sb, 4, "statementDate", s.getStatementDate());
                    appendXmlTag(sb, 4, "planValue", s.getPlanValue());
                    appendXmlTag(sb, 4, "projectedAnnualAmount", s.getProjectedAnnualAmount());
                    appendXmlTag(sb, 4, "yearlyCharges", s.getYearlyCharges());
                    appendXmlTag(sb, 4, "transferValue", s.getTransferValue());
                    appendXmlTag(sb, 4, "amountPaidIn", s.getAmountPaidIn());
                    appendXmlTag(sb, 4, "statementNotes", s.getStatementNotes());
                    sb.append("  </statement>\n");
                }
                sb.append("</statements>\n");
                yield sb.toString();
            }
        };
    }

    private String formatOtherIncomes(List<OtherIncome> otherIncomes, ExportFormat format) {
        return switch (format) {
            case CSV -> {
                StringBuilder sb = new StringBuilder();
                sb.append("id,name,annualAmount,tags,notes\n");
                for (OtherIncome oi : otherIncomes) {
                    List<String> tagNames = extractTagNames(otherIncomeService.getTagsForOtherIncome(oi.getId()));
                    sb.append(csvRow(
                            oi.getId(),
                            oi.getName(),
                            oi.getAnnualAmount(),
                            String.join(", ", tagNames),
                            oi.getNotes()
                    ));
                }
                yield sb.toString();
            }
            case JSON -> {
                List<Map<String, Object>> list = new ArrayList<>();
                for (OtherIncome oi : otherIncomes) {
                    List<String> tagNames = extractTagNames(otherIncomeService.getTagsForOtherIncome(oi.getId()));
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", oi.getId());
                    item.put("name", oi.getName());
                    item.put("annualAmount", oi.getAnnualAmount());
                    item.put("tags", tagNames);
                    item.put("notes", oi.getNotes());
                    list.add(item);
                }
                yield toJson(list);
            }
            case XML -> {
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<otherIncomes>\n");
                for (OtherIncome oi : otherIncomes) {
                    List<String> tagNames = extractTagNames(otherIncomeService.getTagsForOtherIncome(oi.getId()));
                    sb.append("  <otherIncome>\n");
                    appendXmlTag(sb, 4, "id", oi.getId());
                    appendXmlTag(sb, 4, "name", oi.getName());
                    appendXmlTag(sb, 4, "annualAmount", oi.getAnnualAmount());
                    appendXmlTagsList(sb, 4, tagNames);
                    appendXmlTag(sb, 4, "notes", oi.getNotes());
                    sb.append("  </otherIncome>\n");
                }
                sb.append("</otherIncomes>\n");
                yield sb.toString();
            }
        };
    }

    private String formatStatePensions(List<StatePension> statePensions, ExportFormat format) {
        return switch (format) {
            case CSV -> {
                StringBuilder sb = new StringBuilder();
                sb.append("id,name,yearlyAmount,takesEffectYear,notes\n");
                for (StatePension sp : statePensions) {
                    sb.append(csvRow(
                            sp.getId(),
                            sp.getName(),
                            sp.getYearlyAmount(),
                            sp.getTakesEffectYear(),
                            sp.getNotes()
                    ));
                }
                yield sb.toString();
            }
            case JSON -> {
                List<Map<String, Object>> list = new ArrayList<>();
                for (StatePension sp : statePensions) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", sp.getId());
                    item.put("name", sp.getName());
                    item.put("yearlyAmount", sp.getYearlyAmount());
                    item.put("takesEffectYear", sp.getTakesEffectYear());
                    item.put("notes", sp.getNotes());
                    list.add(item);
                }
                yield toJson(list);
            }
            case XML -> {
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<statePensions>\n");
                for (StatePension sp : statePensions) {
                    sb.append("  <statePension>\n");
                    appendXmlTag(sb, 4, "id", sp.getId());
                    appendXmlTag(sb, 4, "name", sp.getName());
                    appendXmlTag(sb, 4, "yearlyAmount", sp.getYearlyAmount());
                    appendXmlTag(sb, 4, "takesEffectYear", sp.getTakesEffectYear());
                    appendXmlTag(sb, 4, "notes", sp.getNotes());
                    sb.append("  </statePension>\n");
                }
                sb.append("</statePensions>\n");
                yield sb.toString();
            }
        };
    }

    private List<String> extractTagNames(List<TagDto> tags) {
        return tags.stream().map(TagDto::name).sorted().toList();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to serialize export JSON", e);
        }
    }

    private static String csvRow(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(values[i]));
        }
        sb.append('\n');
        return sb.toString();
    }

    private static String escapeCsv(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.isEmpty()) return "";
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private static void appendXmlTag(StringBuilder sb, int indent, String tagName, Object value) {
        sb.append(" ".repeat(indent)).append('<').append(tagName).append('>');
        if (value != null) {
            sb.append(escapeXml(value.toString()));
        }
        sb.append("</").append(tagName).append(">\n");
    }

    private static void appendXmlTagsList(StringBuilder sb, int indent, List<String> tags) {
        String pad = " ".repeat(indent);
        if (tags.isEmpty()) {
            sb.append(pad).append("<tags></tags>\n");
            return;
        }
        sb.append(pad).append("<tags>\n");
        for (String tag : tags) {
            appendXmlTag(sb, indent + 2, "tag", tag);
        }
        sb.append(pad).append("</tags>\n");
    }

    private static String escapeXml(String str) {
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static byte[] buildZip(Map<String, String> files) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create export zip archive", e);
        }
        return baos.toByteArray();
    }
}
