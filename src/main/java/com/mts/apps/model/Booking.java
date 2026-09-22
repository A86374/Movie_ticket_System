package com.mts.apps.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Maps to the bookings table. */
public class Booking {

    private int bookingId;
    private Show show;
    private User user;
    private LocalDateTime bookingDate;
    private BigDecimal totalAmount;
    private String bookingStatus;

    public Booking() {
    }

    public Booking(int bookingId, Show show, User user, LocalDateTime bookingDate, BigDecimal totalAmount, String bookingStatus) {
        this.bookingId = bookingId;
        this.show = show;
        this.user = user;
        this.bookingDate = bookingDate;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    @Override
    public String toString() {
        return "Booking{" + "bookingId=" + bookingId + ", " + "show=" + (show == null ? null : show.getShowId()) + ", " + "user=" + (user == null ? null : user.getUserId()) + ", " + "bookingDate=" + bookingDate + ", " + "totalAmount=" + totalAmount + ", " + "bookingStatus=" + bookingStatus + "}";
    }
}
