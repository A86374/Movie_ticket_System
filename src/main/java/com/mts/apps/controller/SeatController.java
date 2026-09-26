package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Seat;
import com.mts.apps.service.ISeatService;
import com.mts.apps.util.InputUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class SeatController {

    private final ISeatService seatService;
    private final Scanner scanner;

    public SeatController(ISeatService seatService, Scanner scanner) {
        this.seatService = seatService;
        this.scanner = scanner;
    }

    // US-07
    public void addSeatRow() throws MtsException {
        String theatreName = InputUtil.readText(scanner, "Theatre name: ");
        String row = InputUtil.readText(scanner, "Row letter (A to Z): ");
        int count = InputUtil.readInt(scanner, "Number of seats in this row: ");
        String type = InputUtil.readText(scanner, "Seat type (SILVER / GOLD / PLATINUM): ");
        BigDecimal price = InputUtil.readDecimal(scanner, "Price per seat: ");

        seatService.addSeatRow(theatreName, row, count, type, price);
        System.out.println("  Row " + row.toUpperCase() + " added with " + count + " seats");
    }

    // US-08
    public void viewSeats() throws MtsException {
        String theatreName = InputUtil.readText(scanner, "Theatre name: ");
        List<Seat> seats = seatService.getSeatsByTheatre(theatreName);

        System.out.printf("%n  %-6s %-9s %8s%n", "SEAT", "TYPE", "PRICE");
        for (Seat s : seats) {
            System.out.printf("  %-6s %-9s %8.2f%n", s.getSeatNumber(), s.getSeatType(), s.getPrice());
        }
        System.out.println("  " + seats.size() + " seats");
    }
}