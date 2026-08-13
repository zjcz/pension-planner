package com.pensionplanner.income;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherIncomeRepository extends JpaRepository<OtherIncome, Long> {

    List<OtherIncome> findByUserIdOrderByNameAsc(Long userId);
}
