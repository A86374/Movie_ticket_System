package com.mts.apps.service;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Movie;

import java.util.List;

public interface IMovieService {

    void addMovie(Movie movie) throws MtsException;

    Movie getMovieByTitle(String title) throws MtsException;

    List<Movie> getAllMovies() throws MtsException;

    void updateMovie(Movie movie) throws MtsException;

    void deleteMovie(String title) throws MtsException;
}
