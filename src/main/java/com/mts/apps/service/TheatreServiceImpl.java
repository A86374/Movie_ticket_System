package com.mts.apps.service;

import com.mts.apps.dao.ISeatDao;
import com.mts.apps.dao.ITheatreDao;
import com.mts.apps.dao.SeatDaoImpl;
import com.mts.apps.dao.TheatreDaoImpl;
import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Theatre;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheatreServiceImpl implements ITheatreService {

    private static final Logger logger = LoggerFactory.getLogger(TheatreServiceImpl.class);

    private final ITheatreDao theatreDao = new TheatreDaoImpl();
    private final ISeatDao seatDao = new SeatDaoImpl();

    @Override
    public void addTheatre(Theatre theatre) throws MtsException {
        validate(theatre);
        try {
            if (theatreDao.getTheatreByName(theatre.getName()) != null) {
                logger.warn("Duplicate theatre rejected: name={}", theatre.getName());
                throw new MtsException("A theatre called " + theatre.getName() + " already exists");
            }

            int id = theatreDao.addTheatre(theatre);
            logger.info("Theatre added: id={}, name={}, city={}, capacity={}",
                    id, theatre.getName(), theatre.getCity(), theatre.getTotalSeats());

        } catch (SQLException e) {
            logger.error("addTheatre failed for name={}", theatre.getName(), e);
            throw new MtsException("Could not add the theatre, please try again", e);
        }
    }

    @Override
    public Theatre getTheatreByName(String name) throws MtsException {
        if (name == null || name.trim().isEmpty()) {
            throw new MtsException("Please enter a theatre name");
        }
        try {
            Theatre theatre = theatreDao.getTheatreByName(name.trim());
            if (theatre == null) {
                throw new MtsException("No theatre found with the name " + name);
            }
            return theatre;

        } catch (SQLException e) {
            logger.error("getTheatreByName failed for name={}", name, e);
            throw new MtsException("Could not load the theatre, please try again", e);
        }
    }

    @Override
    public List<Theatre> getAllTheatres() throws MtsException {
        try {
            List<Theatre> theatres = theatreDao.getAllTheatres();
            if (theatres.isEmpty()) {
                throw new MtsException("No theatres have been added yet");
            }
            return theatres;

        } catch (SQLException e) {
            logger.error("getAllTheatres failed", e);
            throw new MtsException("Could not load the theatre list, please try again", e);
        }
    }

    @Override
    public void updateTheatre(Theatre theatre) throws MtsException {
        validate(theatre);
        if (theatre.getTheatreId() <= 0) {
            throw new MtsException("The theatre to update was not loaded properly");
        }
        try {
            int seats = seatDao.countSeatsByTheatre(theatre);
            if (theatre.getTotalSeats() < seats) {
                throw new MtsException(theatre.getName() + " already has " + seats
                        + " seats, capacity cannot be less than that");
            }
            if (!theatreDao.updateTheatre(theatre)) {
                logger.warn("Update matched no theatre: id={}", theatre.getTheatreId());
                throw new MtsException("That theatre no longer exists");
            }
            logger.info("Theatre updated: id={}, name={}",
                    theatre.getTheatreId(), theatre.getName());

        } catch (SQLException e) {
            logger.error("updateTheatre failed for id={}", theatre.getTheatreId(), e);
            throw new MtsException("Could not update the theatre, please try again", e);
        }
    }

    @Override
    public void deleteTheatre(String name) throws MtsException {
        if (name == null || name.trim().isEmpty()) {
            throw new MtsException("Please enter the theatre name to delete");
        }
        try {
            if (!theatreDao.deleteTheatre(name.trim())) {
                logger.warn("Delete matched no theatre: name={}", name);
                throw new MtsException("No theatre found with the name " + name);
            }
            logger.info("Theatre deleted: name={}", name);

        } catch (SQLException e) {
            logger.error("deleteTheatre failed for name={}", name, e);
            throw new MtsException(
                    "Could not delete the theatre, it may have seats or shows attached", e);
        }
    }

    // US-05 - name, city and a capacity above zero
    private void validate(Theatre theatre) throws MtsException {
        if (theatre == null) {
            throw new MtsException("No theatre details were entered");
        }
        if (theatre.getName() == null || theatre.getName().trim().isEmpty()) {
            throw new MtsException("Theatre name cannot be empty");
        }
        if (theatre.getCity() == null || theatre.getCity().trim().isEmpty()) {
            throw new MtsException("City cannot be empty");
        }
        if (theatre.getTotalSeats() <= 0) {
            throw new MtsException("Total seats must be more than zero");
        }
        theatre.setName(theatre.getName().trim());
        theatre.setCity(theatre.getCity().trim());
        if (theatre.getAddress() != null) {
            theatre.setAddress(theatre.getAddress().trim());
        }
    }
}