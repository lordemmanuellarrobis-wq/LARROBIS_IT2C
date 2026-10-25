
package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {

            System.out.println("==============================================");
            System.out.println("   BUS TICKETING & TERMINAL MANAGEMENT SYSTEM");
            System.out.println("==============================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> register();
                    case "2" -> login();
                    case "0" -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.err.println(
                        "Database error: " + e.getMessage()
                );
            }
        }

        System.out.println("Application closed.");
    }

    private void register() throws SQLException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("Roles: ADMIN, CASHIER, CUSTOMER");
        System.out.print("Choose role: ");
        String role = scanner.nextLine();

        try {
            boolean registered =
                    userDao.registerUser(username, password, role);

            if (registered) {
                System.out.println(
                        "Registration successful! Status: PENDING"
                );
            } else {
                System.out.println("Username already exists.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void login() throws SQLException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            return;
        }

        switch (user.roles().toUpperCase()) {
            case "ADMIN" -> {
                System.out.println("Welcome, Admin!");
                AdminMenu adminMenu =
                        new AdminMenu(userDao, scanner);
                adminMenu.display(user);
            }
            case "CASHIER" ->
                    System.out.println("Cashier menu not connected yet.");
            case "CUSTOMER" ->
                    System.out.println("Customer menu not connected yet.");
            default ->
                    System.out.println("Unknown user role.");
        }
    }
}
