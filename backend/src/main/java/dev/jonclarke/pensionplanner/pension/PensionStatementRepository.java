package dev.jonclarke.pensionplanner.pension;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PensionStatementRepository extends JpaRepository<PensionStatement, Long> {

    List<PensionStatement> findByPensionIdOrderByStatementDateAsc(Long pensionId);

    @Query("SELECT s FROM PensionStatement s WHERE s.statementId = :statementId AND s.pensionId IN "
            + "(SELECT p.pensionId FROM Pension p WHERE p.userId = :userId)")
    Optional<PensionStatement> findByStatementIdAndUserId(@Param("statementId") Long statementId,
                                                          @Param("userId") Long userId);

    Optional<PensionStatement> findByStatementIdAndPensionId(Long statementId, Long pensionId);
}