package dev.jonclarke.pensionplanner.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtherIncomeTagRepository extends JpaRepository<OtherIncomeTag, Long> {

    List<OtherIncomeTag> findByOtherIncomeId(Long otherIncomeId);

    List<OtherIncomeTag> findByTagId(Long tagId);

    void deleteByOtherIncomeId(Long otherIncomeId);
}
