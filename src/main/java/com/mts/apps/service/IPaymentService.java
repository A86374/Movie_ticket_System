package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Payment;

public interface IPaymentService {

    /** US-13 - pays a PENDING booking. The booking becomes CONFIRMED in the same transaction. */
    void pay(Booking booking, String paymentMethod) throws MtsException;

    /** The payment of one booking, or null when it was never paid. Used for US-14. */
    Payment getPaymentOf(Booking booking) throws MtsException;
}