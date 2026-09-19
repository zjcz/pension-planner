package dev.jonclarke.pensionplanner.income;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtherIncomeRepository extends JpaRepository<OtherIncome, Long> {

    List<OtherIncome> findByUserIdOrderByNameAsc(Long userId);

    Optional<OtherIncome> findByIdAndUserId(Long id, Long userId);
}
