package dev.jonclarke.pensionplanner.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "pension_statement_audit")
public class PensionStatementAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditId")
    private Long auditId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "auditTimestamp", nullable = false)
    private Instant auditTimestamp;

    @Column(name = "statementId")
    private Long statementId;

    @Column(name = "pensionId")
    private Long pensionId;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "statementDate")
    private LocalDate statementDate;

    @Column(name = "planValue")
    private Long planValue;

    @Column(name = "projectedAnnualAmount")
    private Long projectedAnnualAmount;

    @Column(name = "yearlyCharges")
    private Long yearlyCharges;

    @Column(name = "transferValue")
    private Long transferValue;

    @Column(name = "amountPaidIn")
    private Long amountPaidIn;

    @Column(name = "statementNotes")
    private String statementNotes;

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

    public Long getStatementId() {
        return statementId;
    }

    public void setStatementId(Long statementId) {
        this.statementId = statementId;
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

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDate statementDate) {
        this.statementDate = statementDate;
    }

    public Long getPlanValue() {
        return planValue;
    }

    public void setPlanValue(Long planValue) {
        this.planValue = planValue;
    }

    public Long getProjectedAnnualAmount() {
        return projectedAnnualAmount;
    }

    public void setProjectedAnnualAmount(Long projectedAnnualAmount) {
        this.projectedAnnualAmount = projectedAnnualAmount;
    }

    public Long getYearlyCharges() {
        return yearlyCharges;
    }

    public void setYearlyCharges(Long yearlyCharges) {
        this.yearlyCharges = yearlyCharges;
    }

    public Long getTransferValue() {
        return transferValue;
    }

    public void setTransferValue(Long transferValue) {
        this.transferValue = transferValue;
    }

    public Long getAmountPaidIn() {
        return amountPaidIn;
    }

    public void setAmountPaidIn(Long amountPaidIn) {
        this.amountPaidIn = amountPaidIn;
    }

    public String getStatementNotes() {
        return statementNotes;
    }

    public void setStatementNotes(String statementNotes) {
        this.statementNotes = statementNotes;
    }
}
