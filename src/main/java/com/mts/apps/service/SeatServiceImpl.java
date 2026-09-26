package com.mts.apps.service;

import com.mts.apps.dao.ISeatDao;
import com.mts.apps.dao.SeatDaoImpl;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Seat;
import com.mts.apps.model.Theatre;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeatServiceImpl implements ISeatService {

    private static final Logger logger = LoggerFactory.getLogger(SeatServiceImpl.class);

    private static final List<String> SEAT_TYPES = List.of("SILVER", "GOLD", "PLATINUM");

    private final ISeatDao seatDao = new SeatDaoImpl();
    private final ITheatreService theatreService = new TheatreServiceImpl();

    // FEATURE 7.1 - a new row, never past the theatre capacity
    @Override
    public void addSeatRow(String theatreName, String rowLetter, int count,
                           String seatType, BigDecimal price) throws MtsException {
        if (rowLetter == null || !rowLetter.trim().toUpperCase().matches("[A-Z]")) {
            throw new MtsException("Row must be a single letter from A to Z");
        }
        if (count < 1) {
            throw new MtsException("A row needs at least one seat");
        }
        String row = rowLetter.trim().toUpperCase();
        String type = checkTypeAndPrice(seatType, price);
        Theatre theatre = theatreService.getTheatreByName(theatreName);

        try {
            List<Seat> existing = seatDao.getSeatsByTheatre(theatre);

            for (Seat seat : existing) {
                if (seat.getSeatNumber().startsWith(row)) {
                    throw new MtsException("Row " + row + " already exists in " + theatre.getName());
                }
            }

            int remaining = theatre.getTotalSeats() - existing.size();
            if (count > remaining) {
                logger.warn("Capacity exceeded: theatre={}, asked={}, remaining={}",
                        theatre.getName(), count, remaining);
                throw new MtsException("Only " + remaining + " more seats can be added to "
                        + theatre.getName());
            }

            for (int i = 1; i <= count; i++) {
                Seat seat = new Seat();
                seat.setTheatre(theatre);
                seat.setSeatNumber(row + i);
                seat.setSeatType(type);
                seat.setPrice(price);
                seatDao.addSeat(seat);
            }
            logger.info("Row {} added to {}: {} {} seats at {}",
                    row, theatre.getName(), count, type, price);

        } catch (SQLException e) {
            logger.error("addSeatRow failed: theatre={}, row={}", theatreName, row, e);
            throw new MtsException("Could not add the seats, please try again", e);
        }
    }

    @Override
    public List<Seat> getSeatsByTheatre(String theatreName) throws MtsException {
        Theatre theatre = theatreService.getTheatreByName(theatreName);
        try {
            List<Seat> seats = seatDao.getSeatsByTheatre(theatre);
            if (seats.isEmpty()) {
                throw new MtsException("No seats have been added to " + theatre.getName() + " yet");
            }
            return seats;

        } catch (SQLException e) {
            logger.error("getSeatsByTheatre failed: theatre={}", theatreName, e);
            throw new MtsException("Could not load the seats, please try again", e);
        }
    }

    @Override
    public void updateSeat(String theatreName, String seatNumber,
                           String seatType, BigDecimal price) throws MtsException {
        String type = checkTypeAndPrice(seatType, price);
        String number = checkSeatNumber(seatNumber);
        Theatre theatre = theatreService.getTheatreByName(theatreName);

        try {
            Seat seat = seatDao.getSeat(theatre, number);
            if (seat == null) {
                throw new MtsException("No seat " + number + " in " + theatre.getName());
            }
            seat.setSeatType(type);
            seat.setPrice(price);
            seatDao.updateSeat(seat);
            logger.info("Seat updated: theatre={}, seat={}, type={}, price={}",
                    theatre.getName(), number, type, price);

        } catch (SQLException e) {
            logger.error("updateSeat failed: theatre={}, seat={}", theatreName, number, e);
            throw new MtsException("Could not update the seat, please try again", e);
        }
    }

    @Override
    public void deleteSeat(String theatreName, String seatNumber) throws MtsException {
        String number = checkSeatNumber(seatNumber);
        Theatre theatre = theatreService.getTheatreByName(theatreName);

        try {
            if (!seatDao.deleteSeat(theatre, number)) {
                throw new MtsException("No seat " + number + " in " + theatre.getName());
            }
            logger.info("Seat deleted: theatre={}, seat={}", theatre.getName(), number);

        } catch (SQLException e) {
            logger.error("deleteSeat failed: theatre={}, seat={}", theatreName, number, e);
            throw new MtsException("Could not delete the seat, it may be held by a booking", e);
        }
    }

    // US-07 - type must be SILVER, GOLD or PLATINUM and price must be positive
    private String checkTypeAndPrice(String seatType, BigDecimal price) throws MtsException {
        if (seatType == null || !SEAT_TYPES.contains(seatType.trim().toUpperCase())) {
            throw new MtsException("Seat type must be SILVER, GOLD or PLATINUM");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MtsException("Price must be more than zero");
        }
        return seatType.trim().toUpperCase();
    }

    private String checkSeatNumber(String seatNumber) throws MtsException {
        if (seatNumber == null || seatNumber.trim().isEmpty()) {
            throw new MtsException("Please enter a seat number");
        }
        return seatNumber.trim().toUpperCase();
    }
}