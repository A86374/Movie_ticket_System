package com.mts.apps.dao;

import com.mts.apps.model.Payment;

import java.sql.SQLException;
import java.util.List;

public interface IPaymentDao {

    int addPayment(Payment payment) throws SQLException;

    Payment getPaymentById(int paymentId) throws SQLException;

    List<Payment> getAllPayments() throws SQLException;

    boolean updatePayment(Payment payment) throws SQLException;

    boolean deletePayment(int paymentId) throws SQLException;

    Payment getPaymentByBooking(int bookingId) throws SQLException;

    boolean updatePaymentStatus(int bookingId, String status) throws SQLException;
}
