package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Show;

import java.time.LocalDate;
import java.util.List;

public interface IShowService {

    /** Schedules a movie in a theatre on a date and slot. End time comes from the movie duration. */
    void scheduleShow(String movieTitle, String theatreName,
                      LocalDate showDate, String showSlot) throws MtsException;

    /** Admin view - every show. */
    List<Show> getAllShows() throws MtsException;

    /** Customer view - only shows of this movie that have not started yet. */
    List<Show> getUpcomingShows(String movieTitle) throws MtsException;

    void deleteShow(String theatreName, LocalDate showDate, String showSlot) throws MtsException;
}