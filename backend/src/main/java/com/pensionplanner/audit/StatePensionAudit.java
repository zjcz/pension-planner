package com.pensionplanner.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "state_pension_audit")
public class StatePensionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditId")
    private Long auditId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "auditTimestamp", nullable = false)
    private Instant auditTimestamp;

    @Column(name = "id")
    private Long id;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "annualAmount")
    private Double annualAmount;

    @Column(name = "notes")
    private String notes;

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Instant getAuditTimestamp() {
        return auditTimestamp;
    }

    public void setAuditTimestamp(Instant auditTimestamp) {
        this.auditTimestamp = auditTimestamp;
    }

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

    public Double getAnnualAmount() {
        return annualAmount;
    }

    public void setAnnualAmount(Double annualAmount) {
        this.annualAmount = annualAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
