package dev.jonclarke.pensionplanner.income;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "state_pension")
public class StatePension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name = "State Pension";

    @Column(name = "yearlyAmount", nullable = false)
    private Long yearlyAmount = 0L;

    @Column(name = "takesEffectYear", nullable = false)
    private Integer takesEffectYear;

    @Column(name = "notes")
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getYearlyAmount() {
        return yearlyAmount;
    }

    public void setYearlyAmount(Long yearlyAmount) {
        this.yearlyAmount = yearlyAmount;
    }

    public Integer getTakesEffectYear() {
        return takesEffectYear;
    }

    public void setTakesEffectYear(Integer takesEffectYear) {
        this.takesEffectYear = takesEffectYear;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
