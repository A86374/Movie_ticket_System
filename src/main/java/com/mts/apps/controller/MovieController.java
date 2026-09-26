package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Movie;
import com.mts.apps.service.IMovieService;
import com.mts.apps.util.InputUtil;

import java.util.List;
import java.util.Scanner;

public class MovieController {

    private final IMovieService movieService;
    private final Scanner scanner;

    public MovieController(IMovieService movieService, Scanner scanner) {
        this.movieService = movieService;
        this.scanner = scanner;
    }

    // US-03
    public void addNewMovie() throws MtsException {
        Movie movie = new Movie();
        movie.setTitle(InputUtil.readText(scanner, "Title: "));
        movie.setLanguage(InputUtil.readText(scanner, "Language: "));
        movie.setGenre(InputUtil.readText(scanner, "Genre: "));
        movie.setDuration(InputUtil.readInt(scanner, "Duration in minutes (30 to 300): "));
        movie.setReleaseDate(InputUtil.readDate(scanner, "Release date (yyyy-mm-dd): "));

        movieService.addMovie(movie);
        System.out.println("  Movie " + movie.getTitle() + " added");
    }

    // US-04
    public void viewAllMovies() throws MtsException {
        List<Movie> movies = movieService.getAllMovies();

        System.out.printf("%n  %-25s %-10s %-12s %8s  %s%n",
                "TITLE", "LANGUAGE", "GENRE", "MINUTES", "RELEASED");
        for (Movie m : movies) {
            System.out.printf("  %-25s %-10s %-12s %8d  %s%n",
                    m.getTitle(), m.getLanguage(), m.getGenre(),
                    m.getDuration(), m.getReleaseDate());
        }
    }
}