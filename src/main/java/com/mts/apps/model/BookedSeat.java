package com.mts.apps.model;

public class BookedSeat {

    private int bookedSeatId;
    private Booking booking;
    private Show show;
    private Seat seat;

    public BookedSeat() {
    }

    public BookedSeat(Booking booking, Show show, Seat seat) {
        this.booking = booking;
        this.show = show;
        this.seat = seat;
    }

    public int getBookedSeatId() {
        return bookedSeatId;
    }

    public void setBookedSeatId(int bookedSeatId) {
        this.bookedSeatId = bookedSeatId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    @Override
    public String toString() {
        return "BookedSeat [bookedSeatId=" + bookedSeatId
                + ", seat=" + (seat == null ? null : seat.getSeatNumber())
                + ", show=" + (show == null ? 0 : show.getShowId()) + "]";
    }
}