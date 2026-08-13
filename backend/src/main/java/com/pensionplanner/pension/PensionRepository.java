package com.pensionplanner.pension;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PensionRepository extends JpaRepository<Pension, Long> {

    List<Pension> findByUserIdOrderByNameAsc(Long userId);

    Optional<Pension> findByPensionIdAndUserId(Long pensionId, Long userId);
}
