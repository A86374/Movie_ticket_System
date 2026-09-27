package com.mts.apps.service;

import com.mts.apps.dao.IPaymentDao;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceImplTest {

    @Mock
    private IPaymentDao paymentDao;              // fake payment DAO

    @InjectMocks
    private PaymentServiceImpl paymentService;   // real service, Mockito calls new PaymentServiceImpl(paymentDao)

    // ---------------------------------------------------------------- pay: happy path

    @Test
    public void pay_pendingBookingByUpi_confirmsIt() throws Exception {
        Booking booking = booking("PENDING");

        paymentService.pay(booking, "upi");

        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);   // catch the Payment the service built
        verify(paymentDao).addPayment(saved.capture());
        Payment payment = saved.getValue();
        assertSame(booking, payment.getBooking());
        assertEquals(new BigDecimal("500.00"), payment.getAmount());   // taken from the booking, never typed in
        assertEquals("UPI", payment.getPaymentMethod());
        assertEquals("SUCCESS", payment.getPaymentStatus());
        assertEquals("CONFIRMED", booking.getBookingStatus());         // the object is updated too
    }

    @Test
    public void pay_everyAllowedMethod_isAccepted() throws Exception {
        String[] methods = {"UPI", " card ", "Cash"};

        for (String method : methods) {
            paymentService.pay(booking("PENDING"), method);
        }

        verify(paymentDao, times(3)).addPayment(any());
    }

    // ---------------------------------------------------------------- pay: rejected

    @Test
    public void pay_unknownMethod_isRejected() throws Exception {
        Booking booking = booking("PENDING");

        MtsException e = assertThrows(MtsException.class, () -> paymentService.pay(booking, "PAYTM"));

        assertEquals("Payment method must be UPI, CARD or CASH", e.getMessage());
        assertEquals("PENDING", booking.getBookingStatus());
        verifyNoInteractions(paymentDao);
    }

    @Test
    public void pay_alreadyConfirmedBooking_isRejected() throws Exception {
        Booking paid = booking("CONFIRMED");

        MtsException e = assertThrows(MtsException.class, () -> paymentService.pay(paid, "UPI"));

        assertEquals("Booking 7 is CONFIRMED and cannot be paid", e.getMessage());   // no double payment
        verifyNoInteractions(paymentDao);
    }

    @Test
    public void pay_cancelledBooking_isRejected() throws Exception {
        Booking cancelled = booking("CANCELLED");

        MtsException e = assertThrows(MtsException.class, () -> paymentService.pay(cancelled, "UPI"));

        assertEquals("Booking 7 is CANCELLED and cannot be paid", e.getMessage());
        verifyNoInteractions(paymentDao);
    }

    @Test
    public void pay_databaseDown_bookingStaysPending() throws Exception {
        Booking booking = booking("PENDING");
        when(paymentDao.addPayment(any())).thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class, () -> paymentService.pay(booking, "UPI"));

        assertEquals("Payment failed, please reload your bookings and try again", e.getMessage());
        assertEquals("PENDING", booking.getBookingStatus());   // not marked CONFIRMED when the payment failed
    }

    // ---------------------------------------------------------------- getPaymentOf

    @Test
    public void getPaymentOf_paidBooking_returnsPayment() throws Exception {
        Booking booking = booking("CONFIRMED");
        Payment payment = new Payment();
        payment.setPaymentStatus("SUCCESS");
        when(paymentDao.getPaymentByBooking(booking)).thenReturn(payment);

        assertSame(payment, paymentService.getPaymentOf(booking));
    }

    @Test
    public void getPaymentOf_unpaidBooking_returnsNull() throws Exception {
        Booking booking = booking("PENDING");
        when(paymentDao.getPaymentByBooking(booking)).thenReturn(null);

        assertNull(paymentService.getPaymentOf(booking));   // the controller shows this as NOT PAID
    }

    @Test
    public void getPaymentOf_databaseDown_givesFriendlyMessage() throws Exception {
        Booking booking = booking("CONFIRMED");
        when(paymentDao.getPaymentByBooking(booking)).thenThrow(new SQLException("Communications link failure"));

        MtsException e = assertThrows(MtsException.class, () -> paymentService.getPaymentOf(booking));

        assertEquals("Could not load the payment, please try again", e.getMessage());
    }

    // ---------------------------------------------------------------- helper

    // booking #7 for 500.00, with the given status
    private Booking booking(String status) {
        Booking b = new Booking();
        b.setBookingId(7);
        b.setTotalAmount(new BigDecimal("500.00"));
        b.setBookingStatus(status);
        return b;
    }
}