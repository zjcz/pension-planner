package dev.jonclarke.pensionplanner.pension;

import dev.jonclarke.pensionplanner.tag.TagDto;

import java.time.LocalDate;
import java.util.List;

public record PensionDto(
        Long pensionId,
        String name,
        LocalDate maturityDate,
        String notes,
        PensionStatus status,
        LocalDate statusDate,
        String color,
        List<TagDto> tags,
        String providerName,
        String policyNumber,
        String workplaceName,
        Long latestProjectedAnnualAmount
) {

    public static PensionDto from(Pension pension, List<TagDto> tags) {
        return from(pension, tags, null);
    }

    public static PensionDto from(Pension pension, List<TagDto> tags, Long latestProjectedAnnualAmount) {
        return new PensionDto(
                pension.getPensionId(),
                pension.getName(),
                pension.getMaturityDate(),
                pension.getNotes(),
                pension.getStatus(),
                pension.getStatusDate(),
                pension.getColor(),
                tags,
                pension.getProviderName(),
                pension.getPolicyNumber(),
                pension.getWorkplaceName(),
                latestProjectedAnnualAmount);
    }
}
