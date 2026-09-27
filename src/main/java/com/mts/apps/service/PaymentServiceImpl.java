package com.mts.apps.service;

import com.mts.apps.dao.IPaymentDao;
import com.mts.apps.dao.PaymentDaoImpl;
import com.mts.apps.exception.BusinessRuleException;
import com.mts.apps.exception.DatabaseException;
import com.mts.apps.exception.MtsException;
import com.mts.apps.exception.ValidationException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Payment;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentServiceImpl implements IPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private static final List<String> METHODS = List.of("UPI", "CARD", "CASH");
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_SUCCESS = "SUCCESS";

    private final IPaymentDao paymentDao;

    // the app uses this one - it creates the real DAO, exactly like before
    public PaymentServiceImpl() {
        this(new PaymentDaoImpl());
    }

    // the tests use this one - they pass in a Mockito fake DAO
    public PaymentServiceImpl(IPaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    // US-13 - only a PENDING booking can be paid, and only once
    @Override
    public void pay(Booking booking, String paymentMethod) throws MtsException {
        String method = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase();
        if (!METHODS.contains(method)) {
            throw new ValidationException("Payment method must be UPI, CARD or CASH");
        }
        if (!STATUS_PENDING.equals(booking.getBookingStatus())) {
            logger.warn("Payment refused, booking is {}: bookingId={}",
                    booking.getBookingStatus(), booking.getBookingId());
            throw new BusinessRuleException("Booking " + booking.getBookingId() + " is "
                    + booking.getBookingStatus() + " and cannot be paid");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(STATUS_SUCCESS);

        try {
            paymentDao.addPayment(payment);
            booking.setBookingStatus(STATUS_CONFIRMED);   // keep the object in step with the database

        } catch (SQLException e) {
            logger.error("pay failed for bookingId={}", booking.getBookingId(), e);
            throw new DatabaseException("Payment failed, please reload your bookings and try again", e);
        }
    }

    @Override
    public Payment getPaymentOf(Booking booking) throws MtsException {
        try {
            return paymentDao.getPaymentByBooking(booking);   // null means not paid

        } catch (SQLException e) {
            logger.error("getPaymentOf failed for bookingId={}", booking.getBookingId(), e);
            throw new DatabaseException("Could not load the payment, please try again", e);
        }
    }
}