package com.mts.apps.dao;

import com.mts.apps.model.Movie;

import java.sql.SQLException;
import java.util.List;

public interface IMovieDao {

    int addMovie(Movie movie) throws SQLException;

    Movie getMovieById(int movieId) throws SQLException;

    List<Movie> getAllMovies() throws SQLException;

    boolean updateMovie(Movie movie) throws SQLException;

    boolean deleteMovie(int movieId) throws SQLException;
}
