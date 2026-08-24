package com.pensionplanner.income;

import com.pensionplanner.tag.TagDto;

import java.util.List;

public record OtherIncomeDto(
        Long id,
        String name,
        Long annualAmount,
        String notes,
        List<TagDto> tags
) {
    public static OtherIncomeDto from(OtherIncome otherIncome, List<TagDto> tags) {
        return new OtherIncomeDto(
                otherIncome.getId(),
                otherIncome.getName(),
                otherIncome.getAnnualAmount(),
                otherIncome.getNotes(),
                tags
        );
    }
}
