package ui;

import dao.UserDao;
import model.User;
import model.UserRoleStatus;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    // START APPLICATION
    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n==============================================");
            System.out.println("Bus Ticketing & Terminal Management System");
            System.out.println("==============================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.println("----------------------------------------------");
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

                System.out.println(
                        "Database error: " + e.getMessage()
                );

            } catch (IllegalArgumentException e) {

                System.out.println(e.getMessage());
            }
        }

        System.out.println("Application closed.");
    }

    // REGISTER
    private void register() throws SQLException {

        System.out.println("\n========== USER REGISTRATION ==========");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        // Public registration creates CUSTOMER accounts only.
        // Admin can assign other roles through the Admin Dashboard.
        UserRoleStatus.Role role = UserRoleStatus.Role.CUSTOMER;

        boolean registered =
                userDao.registerUser(username, password, role);

        if (registered) {

            System.out.println("\nRegistration successful!");
            System.out.println("Role: CUSTOMER");
            System.out.println("Status: PENDING");
            System.out.println("Please wait for admin approval.");

        } else {

            System.out.println("Username already exists.");
        }
    }

    // LOGIN
    private void login() throws SQLException {

        System.out.println("\n========== USER LOGIN ==========");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.username());
        System.out.println("Role: " + user.role());
        System.out.println("Status: " + user.status());

        switch (user.role()) {

            case ADMIN -> {
                System.out.println("Opening Admin Dashboard...");

                AdminMenu adminMenu =
                        new AdminMenu(userDao, scanner);

                adminMenu.display(user);
            }

            case CASHIER -> {
                System.out.println(
                        "Welcome to the Cashier section."
                );
                System.out.println(
                        "Cashier menu is not connected yet."
                );
            }

            case CUSTOMER -> {
                System.out.println(
                        "Welcome to the Customer section."
                );
                System.out.println(
                        "Customer menu is not connected yet."
                );
            }
        }
    }
}