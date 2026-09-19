package dev.jonclarke.pensionplanner.income;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatePensionRepository extends JpaRepository<StatePension, Long> {

    List<StatePension> findByUserIdOrderByNameAsc(Long userId);

    Optional<StatePension> findByIdAndUserId(Long id, Long userId);
}
