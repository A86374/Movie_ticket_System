package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.service.IPaymentService;
import com.mts.apps.util.InputUtil;

import java.util.Scanner;

public class PaymentController {

    private final IPaymentService paymentService;
    private final Scanner scanner;

    public PaymentController(IPaymentService paymentService, Scanner scanner) {
        this.paymentService = paymentService;
        this.scanner = scanner;
    }

    // US-13
    public void payForBooking(Booking booking) throws MtsException {
        System.out.println("  Amount to pay: " + booking.getTotalAmount());
        String method = InputUtil.readText(scanner, "Payment method (UPI / CARD / CASH): ");

        paymentService.pay(booking, method);
        System.out.println("  Payment successful, booking #" + booking.getBookingId()
                + " is " + booking.getBookingStatus());
    }
}