package com.agile.echallan.model;

import com.agile.echallan.enums.PaymentStatus;
import com.agile.echallan.exception.InvalidViolationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Challan {
    private final String challanId;
    private final Violation violation;
    private final double fineAmount;
    private final LocalDateTime issuedTime;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidTime;

    public Challan(Violation violation, double fineAmount) {
        if (violation == null) {
            throw new InvalidViolationException("Violation cannot be null when generating a challan");
        }
        if (fineAmount <= 0) {
            throw new InvalidViolationException("Fine amount must be greater than zero");
        }
        this.challanId = UUID.randomUUID().toString();
        this.violation = violation;
        this.fineAmount = fineAmount;
        this.issuedTime = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.UNPAID;
        this.paidTime = null;
    }

    public String getChallanId() {
        return challanId;
    }

    public Violation getViolation() {
        return violation;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public LocalDateTime getIssuedTime() {
        return issuedTime;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void markPaid() {
        this.paymentStatus = PaymentStatus.PAID;
        this.paidTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Challan)) return false;
        Challan challan = (Challan) o;
        return challanId.equals(challan.challanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(challanId);
    }

    @Override
    public String toString() {
        return "Challan{id='" + challanId + "', vehicle='" + violation.getVehicleNumber() + "', fine=" + fineAmount
                + ", status=" + paymentStatus + "}";
    }
}
