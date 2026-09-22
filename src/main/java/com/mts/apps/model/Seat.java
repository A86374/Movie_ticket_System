package com.mts.apps.model;

import java.math.BigDecimal;

/** Maps to the seats table. */
public class Seat {

    private int seatId;
    private Theatre theatre;
    private String seatNumber;
    private String seatType;
    private BigDecimal price;

    public Seat() {
    }

    public Seat(int seatId, Theatre theatre, String seatNumber, String seatType, BigDecimal price) {
        this.seatId = seatId;
        this.theatre = theatre;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.price = price;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Seat{" + "seatId=" + seatId + ", " + "theatre=" + (theatre == null ? null : theatre.getTheatreId()) + ", " + "seatNumber=" + seatNumber + ", " + "seatType=" + seatType + ", " + "price=" + price + "}";
    }
}
