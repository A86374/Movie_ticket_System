package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.Booking;
import com.mts.apps.model.Show;
import com.mts.apps.model.User;
import com.mts.apps.service.BookingServiceImpl;
import com.mts.apps.service.IBookingService;
import com.mts.apps.service.IMovieService;
import com.mts.apps.service.IPaymentService;
import com.mts.apps.service.ISeatService;
import com.mts.apps.service.IShowService;
import com.mts.apps.service.ITheatreService;
import com.mts.apps.service.IUserService;
import com.mts.apps.service.MovieServiceImpl;
import com.mts.apps.service.PaymentServiceImpl;
import com.mts.apps.service.SeatServiceImpl;
import com.mts.apps.service.ShowServiceImpl;
import com.mts.apps.service.TheatreServiceImpl;
import com.mts.apps.service.UserServiceImpl;
import com.mts.apps.util.InputUtil;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    private static final String ROLE_ADMIN = "ADMIN";

    private final Scanner scanner;
    private final UserController userController;
    private final MovieController movieController;
    private final TheatreController theatreController;
    private final SeatController seatController;
    private final ShowController showController;
    private final BookingController bookingController;
    private final PaymentController paymentController;

    public AppController(Scanner scanner,
                         UserController userController,
                         MovieController movieController,
                         TheatreController theatreController,
                         SeatController seatController,
                         ShowController showController,
                         BookingController bookingController,
                         PaymentController paymentController) {
        this.scanner = scanner;
        this.userController = userController;
        this.movieController = movieController;
        this.theatreController = theatreController;
        this.seatController = seatController;
        this.showController = showController;
        this.bookingController = bookingController;
        this.paymentController = paymentController;
    }

    public static void main(String[] args) {
        logger.info("Movie Ticket System started");

        try (Scanner scanner = new Scanner(System.in)) {
            // each service creates its own DAO, and each DAO creates its own JdbcUtil
            IUserService userService = new UserServiceImpl();
            IMovieService movieService = new MovieServiceImpl();
            ITheatreService theatreService = new TheatreServiceImpl();
            ISeatService seatService = new SeatServiceImpl();
            IShowService showService = new ShowServiceImpl();
            IBookingService bookingService = new BookingServiceImpl();
            IPaymentService paymentService = new PaymentServiceImpl();

            // each controller is handed its service, it never creates one itself
            AppController app = new AppController(
                    scanner,
                    new UserController(userService, scanner),
                    new MovieController(movieService, scanner),
                    new TheatreController(theatreService, scanner),
                    new SeatController(seatService, scanner),
                    new ShowController(showService, scanner),
                    new BookingController(bookingService, paymentService, scanner),
                    new PaymentController(paymentService, scanner));

            app.start();
        }

        logger.info("Movie Ticket System stopped");
    }

    // ---------------------------------------------------------------- start menu

    public void start() {
        System.out.println("\n===== MOVIE TICKET SYSTEM =====");
        while (true) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            int choice = InputUtil.readInt(scanner, "Choose: ");

            if (choice == 0) {
                System.out.println("  Goodbye");
                return;
            }
            try {
                switch (choice) {
                    case 1 -> userController.register();
                    case 2 -> {
                        User user = userController.login();
                        if (ROLE_ADMIN.equals(user.getRole())) {
                            adminMenu();
                        } else {
                            customerMenu(user);
                        }
                    }
                    default -> System.out.println("  Please choose one of the options above");
                }
            } catch (MtsException e) {
                System.out.println("  " + e.getMessage());
            } catch (RuntimeException e) {
                logger.error("Unexpected error in start menu, option {}", choice, e);
                System.out.println("  Something went wrong, please try again");
            }
        }
    }

    // ---------------------------------------------------------------- admin

    private void adminMenu() {
        while (true) {
            System.out.println("\n----- ADMIN MENU -----");
            System.out.println("1. Add movie");
            System.out.println("2. View movies");
            System.out.println("3. Add theatre");
            System.out.println("4. View theatres");
            System.out.println("5. Add a row of seats");
            System.out.println("6. View seats of a theatre");
            System.out.println("7. Schedule a show");
            System.out.println("8. View all shows");
            System.out.println("9. View all bookings with payment status");
            System.out.println("0. Logout");
            int choice = InputUtil.readInt(scanner, "Choose: ");

            if (choice == 0) {
                return;
            }
            try {
                switch (choice) {
                    case 1 -> movieController.addNewMovie();
                    case 2 -> movieController.viewAllMovies();
                    case 3 -> theatreController.addNewTheatre();
                    case 4 -> theatreController.viewAllTheatres();
                    case 5 -> seatController.addSeatRow();
                    case 6 -> seatController.viewSeats();
                    case 7 -> showController.scheduleShow();
                    case 8 -> showController.viewAllShows();
                    case 9 -> bookingController.viewAllBookings();
                    default -> System.out.println("  Please choose one of the options above");
                }
            } catch (MtsException e) {
                System.out.println("  " + e.getMessage());
            } catch (RuntimeException e) {
                logger.error("Unexpected error in admin menu, option {}", choice, e);
                System.out.println("  Something went wrong, please try again");
            }
        }
    }

    // ---------------------------------------------------------------- customer

    private void customerMenu(User user) {
        while (true) {
            System.out.println("\n----- CUSTOMER MENU -----");
            System.out.println("1. View movies");
            System.out.println("2. View upcoming shows of a movie");
            System.out.println("3. Book tickets");
            System.out.println("4. Pay for a pending booking");
            System.out.println("5. My bookings");
            System.out.println("6. Cancel a booking");
            System.out.println("0. Logout");
            int choice = InputUtil.readInt(scanner, "Choose: ");

            if (choice == 0) {
                return;
            }
            try {
                switch (choice) {
                    case 1 -> movieController.viewAllMovies();
                    case 2 -> showController.viewUpcomingShows();
                    case 3 -> bookTickets(user);
                    case 4 -> paymentController.payForBooking(bookingController.choosePendingBooking(user));
                    case 5 -> bookingController.viewMyBookings(user);
                    case 6 -> bookingController.cancelBooking(user);
                    default -> System.out.println("  Please choose one of the options above");
                }
            } catch (MtsException e) {
                System.out.println("  " + e.getMessage());
            } catch (RuntimeException e) {
                logger.error("Unexpected error in customer menu, option {}, user {}",
                        choice, user.getEmail(), e);
                System.out.println("  Something went wrong, please try again");
            }
        }
    }

    // 9.2 booking flow, then 9.3 pay right away or later
    private void bookTickets(User user) throws MtsException {
        Show show = showController.chooseUpcomingShow();
        Booking booking = bookingController.bookSeats(user, show);

        if (InputUtil.readYes(scanner, "Pay now? (y/n): ")) {
            try {
                paymentController.payForBooking(booking);
                return;
            } catch (MtsException e) {
                System.out.println("  " + e.getMessage());
            }
        }
        System.out.println("  Booking #" + booking.getBookingId()
                + " is saved as PENDING, you can pay later from the menu");
    }
}