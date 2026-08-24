package com.pensionplanner.pension;

import com.pensionplanner.tag.TagDto;

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
        List<TagDto> tags
) {

    public static PensionDto from(Pension pension, List<TagDto> tags) {
        return new PensionDto(
                pension.getPensionId(),
                pension.getName(),
                pension.getMaturityDate(),
                pension.getNotes(),
                pension.getStatus(),
                pension.getStatusDate(),
                pension.getColor(),
                tags);
    }
}
