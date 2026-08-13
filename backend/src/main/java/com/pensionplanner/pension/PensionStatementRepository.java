package com.pensionplanner.pension;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PensionStatementRepository extends JpaRepository<PensionStatement, Long> {

    List<PensionStatement> findByPensionIdOrderByStatementDateAsc(Long pensionId);

    Optional<PensionStatement> findByStatementIdAndUserId(Long statementId, Long userId);
}
