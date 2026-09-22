package com.mts.apps.service;

import com.mts.apps.model.Payment;
import java.util.List;

/** Payments and payment status. */
public interface IPaymentService {

    /** Method must be UPI, CARD or CASH. Rejects a cancelled or already paid booking. Saves the payment as SUCCESS and sets the booking CONFIRMED. */
    Payment makePayment(int userId, int bookingId, String paymentMethod) throws MtsException;

    /** One payment. */
    Payment getPaymentById(int paymentId) throws MtsException;

    /** Payment of a booking, or null if not paid yet. */
    Payment getPaymentByBooking(int bookingId) throws MtsException;

    /** Every payment with its status. */
    List<Payment> getAllPayments() throws MtsException;

}
