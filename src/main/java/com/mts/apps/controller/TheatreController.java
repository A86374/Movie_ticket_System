package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Theatre;
import com.mts.apps.service.ITheatreService;
import com.mts.apps.util.InputUtil;

import java.util.List;
import java.util.Scanner;

public class TheatreController {

    private final ITheatreService theatreService;
    private final Scanner scanner;

    public TheatreController(ITheatreService theatreService, Scanner scanner) {
        this.theatreService = theatreService;
        this.scanner = scanner;
    }

    // US-05
    public void addNewTheatre() throws MtsException {
        Theatre theatre = new Theatre();
        theatre.setName(InputUtil.readText(scanner, "Theatre name: "));
        theatre.setCity(InputUtil.readText(scanner, "City: "));
        theatre.setAddress(InputUtil.readText(scanner, "Address: "));
        theatre.setTotalSeats(InputUtil.readInt(scanner, "Total seat capacity: "));

        theatreService.addTheatre(theatre);
        System.out.println("  Theatre " + theatre.getName() + " added");
    }

    // US-06
    public void viewAllTheatres() throws MtsException {
        List<Theatre> theatres = theatreService.getAllTheatres();

        System.out.printf("%n  %-20s %-12s %8s  %s%n", "NAME", "CITY", "CAPACITY", "ADDRESS");
        for (Theatre t : theatres) {
            System.out.printf("  %-20s %-12s %8d  %s%n",
                    t.getName(), t.getCity(), t.getTotalSeats(), t.getAddress());
        }
    }
}