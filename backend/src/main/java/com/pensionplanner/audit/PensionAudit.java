package com.pensionplanner.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "pension_audit")
public class PensionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditId")
    private Long auditId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "auditTimestamp", nullable = false)
    private Instant auditTimestamp;

    @Column(name = "pensionId")
    private Long pensionId;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "maturityDate")
    private LocalDate maturityDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "status")
    private String status;

    @Column(name = "statusDate")
    private LocalDate statusDate;

    @Column(name = "color")
    private String color;

    @Column(name = "providerName")
    private String providerName;

    @Column(name = "policyNumber")
    private String policyNumber;

    @Column(name = "workplaceName")
    private String workplaceName;

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

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(LocalDate statusDate) {
        this.statusDate = statusDate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getWorkplaceName() {
        return workplaceName;
    }

    public void setWorkplaceName(String workplaceName) {
        this.workplaceName = workplaceName;
    }
}
