package dev.jonclarke.pensionplanner.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "other_income_tag")
public class OtherIncomeTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "otherIncomeId", nullable = false)
    private Long otherIncomeId;

    @Column(name = "tagId", nullable = false)
    private Long tagId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOtherIncomeId() {
        return otherIncomeId;
    }

    public void setOtherIncomeId(Long otherIncomeId) {
        this.otherIncomeId = otherIncomeId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
