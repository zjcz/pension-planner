package com.pensionplanner.pension;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "pension_statement")
public class PensionStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statementId")
    private Long statementId;

    @Column(name = "pensionId", nullable = false)
    private Long pensionId;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "statementDate", nullable = false)
    private Instant statementDate;

    @Column(name = "planValue", nullable = false)
    private Double planValue;

    @Column(name = "projectedAnnualAmount", nullable = false)
    private Double projectedAnnualAmount;

    @Column(name = "yearlyCharges")
    private Double yearlyCharges;

    @Column(name = "transferValue")
    private Double transferValue;

    @Column(name = "amountPaidIn")
    private Double amountPaidIn;

    @Column(name = "statementNotes")
    private String statementNotes;

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

    public Instant getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(Instant statementDate) {
        this.statementDate = statementDate;
    }

    public Double getPlanValue() {
        return planValue;
    }

    public void setPlanValue(Double planValue) {
        this.planValue = planValue;
    }

    public Double getProjectedAnnualAmount() {
        return projectedAnnualAmount;
    }

    public void setProjectedAnnualAmount(Double projectedAnnualAmount) {
        this.projectedAnnualAmount = projectedAnnualAmount;
    }

    public Double getYearlyCharges() {
        return yearlyCharges;
    }

    public void setYearlyCharges(Double yearlyCharges) {
        this.yearlyCharges = yearlyCharges;
    }

    public Double getTransferValue() {
        return transferValue;
    }

    public void setTransferValue(Double transferValue) {
        this.transferValue = transferValue;
    }

    public Double getAmountPaidIn() {
        return amountPaidIn;
    }

    public void setAmountPaidIn(Double amountPaidIn) {
        this.amountPaidIn = amountPaidIn;
    }

    public String getStatementNotes() {
        return statementNotes;
    }

    public void setStatementNotes(String statementNotes) {
        this.statementNotes = statementNotes;
    }
}
