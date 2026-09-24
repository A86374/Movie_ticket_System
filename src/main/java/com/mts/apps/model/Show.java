package com.mts.apps.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Maps to the shows table. */
public class Show {

    private int showId;
    private Theatre theatre;
    private Movie movie;
    private String showSlot;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public Show() {
    }

    public Show(int showId, Theatre theatre, Movie movie, LocalDate showDate, LocalTime startTime, LocalTime endTime) {
        this.showId = showId;
        this.theatre = theatre;
        this.movie = movie;
        this.showDate = showDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getShowSlot() {return showSlot;}

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public void setShowSlot(String showSlot) {this.showSlot = showSlot;}

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Show{" + "showId=" + showId + ", " + "theatre=" + (theatre == null ? null : theatre.getTheatreId()) + ", " + "movie=" + (movie == null ? null : movie.getMovieId()) + ", " + "showDate=" + showDate + ", " + "startTime=" + startTime + ", " + "endTime=" + endTime + "}";
    }
}
