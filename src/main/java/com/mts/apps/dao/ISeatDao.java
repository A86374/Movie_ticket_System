package com.mts.apps.dao;

import com.mts.apps.model.Seat;
import com.mts.apps.model.Theatre;

import java.sql.SQLException;
import java.util.List;

public interface ISeatDao {

    int addSeat(Seat seat) throws SQLException;

    Seat getSeat(Theatre theatre, String seatNumber) throws SQLException;

    List<Seat> getSeatsByTheatre(Theatre theatre) throws SQLException;

    int countSeatsByTheatre(Theatre theatre) throws SQLException;

    boolean updateSeat(Seat seat) throws SQLException;

    boolean deleteSeat(Theatre theatre, String seatNumber) throws SQLException;
}