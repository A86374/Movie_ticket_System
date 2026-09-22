# 🎬 Movie Ticket System

A simple movie ticket booking application built with Java, JDBC and MySQL as part of the Revature training program. An admin manages movies, theatres, seats and show timings, and customers book seats, pay and cancel their bookings.

> 🔒 This is a private repository. Access is by invitation only.

## Features

**Admin**
- Add and view movies
- Add and view theatres with their seat capacity
- Add seats to a theatre row by row with type and price
- Schedule show timings with no overlapping shows in a theatre
- View every booking with its payment status

**Customer**
- Register and log in
- View movies and their show timings
- See available seats and book up to 10 seats
- Pay with UPI, CARD or CASH
- View and cancel bookings

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 |
| Database | MySQL 8 |
| Database Access | JDBC |
| Build Tool | Maven |
| Architecture | Layered Architecture |
| Version Control | Git and GitHub |

## Database Tables

| Table | Purpose |
|---|---|
| users | Admins and customers with their role |
| movies | Movie details and duration |
| theatres | Theatre details and seat capacity |
| seats | Seats of a theatre with type and price |
| shows | Movie timings in a theatre |
| bookings | One booking with total and status |
| booked_seats | Seats inside a booking |
| payments | Payment of a booking with its status |

## Project Structure

```
src/main/java/com/mts/apps/
├── controller/   Main class and the admin and customer menus
├── service/      Business rules
├── dao/          Database queries
├── model/        Classes that map to the tables
├── exception/    MtsException for business rule errors
└── util/         JdbcUtil for the database connection
```

Every request flows the same way:

```
controller  →  service  →  dao  →  MySQL
```

## Getting Started

**1. Clone the repository**

You need to be added as a collaborator first. Accept the invitation from GitHub, then:

```bash
git clone https://github.com/A86374/Movie_ticket_System.git
```

Git will ask you to sign in with the GitHub account that received the invitation.

**2. Create the database**

Run `movie_ticket_system.sql` in MySQL Workbench. It creates the `movie_ticket_system` database with all tables and sample data.

**3. Set your MySQL login**

Update `URL`, `USER` and `PASSWORD` in `util/JdbcUtil.java`.

**4. Run**

Open the project in IntelliJ IDEA as a Maven project, let the dependencies download, and run the main class in the `controller` package.

Sample admin login: `admin@mts.com` / `admin123`

## Booking Life Cycle

```
PENDING  →  CONFIRMED (payment SUCCESS)
   ↓
CANCELLED (payment REFUNDED if it was paid)
```
