package com.mts.apps.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Maps to the payments table. */
public class Payment {

    private int paymentId;
    private Booking booking;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;

    public Payment() {
    }

    public Payment(int paymentId, Booking booking, BigDecimal amount, String paymentMethod, String paymentStatus, LocalDateTime paymentDate) {
        this.paymentId = paymentId;
        this.booking = booking;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return "Payment{" + "paymentId=" + paymentId + ", " + "booking=" + (booking == null ? null : booking.getBookingId()) + ", " + "amount=" + amount + ", " + "paymentMethod=" + paymentMethod + ", " + "paymentStatus=" + paymentStatus + ", " + "paymentDate=" + paymentDate + "}";
    }
}
