package com.mts.apps.model;

/** Maps to the booked_seats table. */
public class BookedSeat {

    private int bookedSeatId;
    private Booking booking;
    private Seat seat;

    public BookedSeat() {
    }

    public BookedSeat(int bookedSeatId, Booking booking, Seat seat) {
        this.bookedSeatId = bookedSeatId;
        this.booking = booking;
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

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    @Override
    public String toString() {
        return "BookedSeat{" + "bookedSeatId=" + bookedSeatId + ", " + "booking=" + (booking == null ? null : booking.getBookingId()) + ", " + "seat=" + (seat == null ? null : seat.getSeatId()) + "}";
    }
}
