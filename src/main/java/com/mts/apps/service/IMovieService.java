package com.mts.apps.service;

import com.mts.apps.model.Movie;
import java.util.List;

/** Movie management. */
public interface IMovieService {

    /** Duration must be between 30 and 300 minutes. */
    int addMovie(Movie movie) throws MtsException;

    /** One movie. */
    Movie getMovieById(int movieId) throws MtsException;

    /** Every movie. */
    List<Movie> getAllMovies() throws MtsException;

    /** Same checks as add. */
    boolean updateMovie(Movie movie) throws MtsException;

    /** Removes a movie that has no shows. */
    boolean deleteMovie(int movieId) throws MtsException;

}
