package dev.jonclarke.pensionplanner.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatePensionAuditRepository extends JpaRepository<StatePensionAudit, Long> {

    void deleteByUserId(Long userId);

    List<StatePensionAudit> findByIdOrderByAuditTimestampDesc(Long id);
}
