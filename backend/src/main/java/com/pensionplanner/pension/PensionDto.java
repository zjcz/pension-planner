package com.pensionplanner.pension;

import java.time.LocalDate;

public record PensionDto(
        Long pensionId,
        String name,
        LocalDate maturityDate,
        String notes,
        PensionStatus status,
        LocalDate statusDate,
        String color
) {

    public static PensionDto from(Pension pension) {
        return new PensionDto(
                pension.getPensionId(),
                pension.getName(),
                pension.getMaturityDate(),
                pension.getNotes(),
                pension.getStatus(),
                pension.getStatusDate(),
                pension.getColor());
    }
}
