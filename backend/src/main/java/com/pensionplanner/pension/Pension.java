package com.pensionplanner.pension;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "pension")
public class Pension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pensionId")
    private Long pensionId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "maturityDate", nullable = false)
    private Instant maturityDate;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PensionStatus status;

    @Column(name = "statusDate")
    private Instant statusDate;

    @Column(name = "color")
    private String color;

    public Long getPensionId() {
        return pensionId;
    }

    public void setPensionId(Long pensionId) {
        this.pensionId = pensionId;
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

    public Instant getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(Instant maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PensionStatus getStatus() {
        return status;
    }

    public void setStatus(PensionStatus status) {
        this.status = status;
    }

    public Instant getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Instant statusDate) {
        this.statusDate = statusDate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
