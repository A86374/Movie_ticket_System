package com.mts.apps.service;

import com.mts.apps.model.Theatre;
import java.util.List;

/** Theatre management. */
public interface ITheatreService {

    /** Total seats must be more than zero. */
    int addTheatre(Theatre theatre) throws MtsException;

    /** One theatre. */
    Theatre getTheatreById(int theatreId) throws MtsException;

    /** Every theatre. */
    List<Theatre> getAllTheatres() throws MtsException;

    /** Total seats cannot drop below the seats already added. */
    boolean updateTheatre(Theatre theatre) throws MtsException;

    /** Removes a theatre that has no seats or shows. */
    boolean deleteTheatre(int theatreId) throws MtsException;

}
