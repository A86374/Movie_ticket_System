package com.mts.apps.dao;

import com.mts.apps.model.Booking;
import com.mts.apps.model.Payment;

import java.sql.SQLException;
import java.util.List;

public interface IPaymentDao {

    // saves the payment and confirms the booking in one transaction
    int addPayment(Payment payment) throws SQLException;

    Payment getPaymentByBooking(Booking booking) throws SQLException;

    List<Payment> getAllPayments() throws SQLException;
}