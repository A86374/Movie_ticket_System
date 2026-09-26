package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Payment;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Show;
import com.mts.apps.model.User;
import com.mts.apps.service.IBookingService;
import com.mts.apps.service.IPaymentService;
import com.mts.apps.util.InputUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class BookingController {

    private final IBookingService bookingService;
    private final IPaymentService paymentService;   // only to show the payment status
    private final Scanner scanner;

    public BookingController(IBookingService bookingService, IPaymentService paymentService,
                             Scanner scanner) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.scanner = scanner;
    }

    // US-11 and US-12
    public Booking bookSeats(User user, Show show) throws MtsException {
        List<Seat> seats = bookingService.getAvailableSeats(show);

        System.out.printf("%n  Available seats%n  %-6s %-9s %8s%n", "SEAT", "TYPE", "PRICE");
        for (Seat s : seats) {
            System.out.printf("  %-6s %-9s %8.2f%n", s.getSeatNumber(), s.getSeatType(), s.getPrice());
        }

        // "A1, A2" and "A1 A2" both work
        String line = InputUtil.readText(scanner, "Seat numbers, for example A1, A2 (max 10): ");
        Booking booking = bookingService.bookSeats(user, show, Arrays.asList(line.split("[,\\s]+")));

        System.out.println("  Booking #" + booking.getBookingId() + " created, total "
                + booking.getTotalAmount() + ", status " + booking.getBookingStatus());
        return booking;
    }

    // US-15
    public void viewMyBookings(User user) throws MtsException {
        for (Booking b : bookingService.getMyBookings(user)) {
            printBooking(b, false);
        }
    }

    // pay later - the customer picks one of their PENDING bookings
    public Booking choosePendingBooking(User user) throws MtsException {
        List<Booking> pending = bookingService.getMyBookingsByStatus(user, "PENDING");
        for (Booking b : pending) {
            printBooking(b, false);
        }
        return pickById(pending, "Booking number to pay for: ");
    }

    // US-16
    public void cancelBooking(User user) throws MtsException {
        List<Booking> active = new ArrayList<>();
        for (Booking b : bookingService.getMyBookings(user)) {
            if (!"CANCELLED".equals(b.getBookingStatus())) {
                active.add(b);
                printBooking(b, false);
            }
        }
        if (active.isEmpty()) {
            throw new MtsException("You have no bookings to cancel");
        }

        Booking booking = pickById(active, "Booking number to cancel: ");
        boolean wasPaid = "CONFIRMED".equals(booking.getBookingStatus());

        bookingService.cancelBooking(user, booking);
        System.out.println("  Booking #" + booking.getBookingId() + " cancelled, seats released"
                + (wasPaid ? ", payment refunded" : ""));
    }

    // US-14 - admin sees every booking with its payment status
    public void viewAllBookings() throws MtsException {
        for (Booking b : bookingService.getAllBookings()) {
            printBooking(b, true);
        }
    }

    // ---------------------------------------------------------------- helpers

    private void printBooking(Booking b, boolean showCustomer) throws MtsException {
        Show s = b.getShow();
        System.out.printf("%n  #%d  %s | %s | %s %s %s%n",
                b.getBookingId(), s.getMovie().getTitle(), s.getTheatre().getName(),
                s.getShowDate(), s.getShowSlot(), s.getStartTime());
        if (showCustomer) {
            System.out.printf("       Customer: %s (%s)%n", b.getUser().getName(), b.getUser().getEmail());
        }
        System.out.printf("       Seats: %s | Total: %s | Booking: %s | Payment: %s%n",
                seatList(b), b.getTotalAmount(), b.getBookingStatus(), paymentStatus(b));
    }

    private String seatList(Booking b) throws MtsException {
        List<String> numbers = new ArrayList<>();
        for (Seat seat : bookingService.getSeatsOfBooking(b)) {
            numbers.add(seat.getSeatNumber());
        }
        return numbers.isEmpty() ? "-" : String.join(", ", numbers);   // cancelled bookings have none
    }

    private String paymentStatus(Booking b) throws MtsException {
        Payment payment = paymentService.getPaymentOf(b);
        return payment == null ? "NOT PAID" : payment.getPaymentStatus();
    }

    // the booking number shown on screen, looked up only among the bookings just listed
    private Booking pickById(List<Booking> bookings, String prompt) throws MtsException {
        int id = InputUtil.readInt(scanner, prompt);
        for (Booking b : bookings) {
            if (b.getBookingId() == id) {
                return b;
            }
        }
        throw new MtsException("No booking #" + id + " in the list above");
    }
}