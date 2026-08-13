package com.pensionplanner.income;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatePensionRepository extends JpaRepository<StatePension, Long> {

    Optional<StatePension> findByUserId(Long userId);
}
