package com.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtherIncomeAuditRepository extends JpaRepository<OtherIncomeAudit, Long> {
}
