package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Show;
import com.mts.apps.service.IShowService;
import com.mts.apps.util.InputUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ShowController {

    private final IShowService showService;
    private final Scanner scanner;

    public ShowController(IShowService showService, Scanner scanner) {
        this.showService = showService;
        this.scanner = scanner;
    }

    // US-09
    public void scheduleShow() throws MtsException {
        String title = InputUtil.readText(scanner, "Movie title: ");
        String theatreName = InputUtil.readText(scanner, "Theatre name: ");
        LocalDate date = InputUtil.readDate(scanner, "Show date (yyyy-mm-dd): ");
        System.out.println("  Slots: MORNING 08:30 | MATINEE 12:00 | FIRST_SHOW 18:30 | SECOND_SHOW 21:00");
        String slot = InputUtil.readText(scanner, "Slot: ");

        showService.scheduleShow(title, theatreName, date, slot);
        System.out.println("  Show scheduled");
    }

    // US-10 - admin sees every show
    public void viewAllShows() throws MtsException {
        printShows(showService.getAllShows());
    }

    // US-10 - customer sees only the upcoming shows of one movie
    public void viewUpcomingShows() throws MtsException {
        String title = InputUtil.readText(scanner, "Movie title: ");
        printShows(showService.getUpcomingShows(title));
    }

    // first step of booking - the customer picks one show from the list
    public Show chooseUpcomingShow() throws MtsException {
        String title = InputUtil.readText(scanner, "Movie title: ");
        List<Show> shows = showService.getUpcomingShows(title);
        printShows(shows);

        int choice = InputUtil.readInt(scanner, "Choose a show number: ");
        if (choice < 1 || choice > shows.size()) {
            throw new MtsException("Please choose a number between 1 and " + shows.size());
        }
        return shows.get(choice - 1);
    }

    private void printShows(List<Show> shows) {
        System.out.println();
        for (int i = 0; i < shows.size(); i++) {
            Show s = shows.get(i);
            System.out.printf("  %2d. %-22s %-18s %s  %-11s %s - %s%n",
                    i + 1, s.getMovie().getTitle(), s.getTheatre().getName(),
                    s.getShowDate(), s.getShowSlot(), s.getStartTime(), s.getEndTime());
        }
    }
}