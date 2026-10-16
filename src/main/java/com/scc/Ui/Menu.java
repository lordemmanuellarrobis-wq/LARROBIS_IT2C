
package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Menu {

    private final Scanner sc = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n==========================================");
            System.out.println("   BUS TICKETING & TERMINAL MANAGEMENT");
            System.out.println("                TerminalGo");
            System.out.println("==========================================");
            System.out.println("1. Login");
            System.out.println("2. Register Account");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> login();
                case "2" -> register();
                case "3" -> {
                    System.out.println("Thank you for using TerminalGo!");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void register() {
        System.out.println("\n========== REGISTER ACCOUNT ==========");

        try {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            if (username.isBlank()) {
                System.out.println("Username cannot be empty.");
                return;
            }

            System.out.print("Enter password: ");
            String password = sc.nextLine();

            if (password.isBlank()) {
                System.out.println("Password cannot be empty.");
                return;
            }

            System.out.println("\nSelect your role:");
            System.out.println("1. ADMIN");
            System.out.println("2. CASHIER");
            System.out.println("3. CUSTOMER");
            System.out.print("Choose a role: ");

            String choice = sc.nextLine().trim();

            String role = switch (choice) {
                case "1" -> "ADMIN";
                case "2" -> "CASHIER";
                case "3" -> "CUSTOMER";
                default -> null;
            };

            if (role == null) {
                System.out.println("Invalid role selection.");
                return;
            }

            if (userDao.usernameExists(username)) {
                System.out.println(
                        "Username already exists. Choose another."
                );
                return;
            }

            if (userDao.registerUser(username, password, role)) {
                System.out.println("\nRegistration successful!");
                System.out.println("Username: " + username);
                System.out.println("Role: " + role);
                System.out.println("Status: PENDING");
                System.out.println(
                        "Your account is waiting for approval."
                );
            } else {
                System.out.println("Registration failed.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Registration database error: " + e.getMessage()
            );
        }
    }

    private void login() {
        System.out.println("\n============== LOGIN ==============");

        try {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();

            System.out.print("Enter password: ");
            String password = sc.nextLine();

            User user = userDao.login(username, password);

            if (user == null) {
                System.out.println("Invalid username or password.");
                return;
            }

            System.out.println("\nLogin successful!");
            System.out.println("Welcome, " + user.username());
            System.out.println("Role: " + user.roles());
            System.out.println("Status: " + user.status());

            if ("PENDING".equalsIgnoreCase(user.status())) {
                System.out.println(
                        "Notice: Your account is pending approval."
                );
            }

            userMenu(user);

        } catch (SQLException e) {
            System.out.println("Login database error: " + e.getMessage());
        }
    }

    private void userMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n================================");
            System.out.println("           USER MENU");
            System.out.println("================================");
            System.out.println("User: " + user.username());
            System.out.println("Role: " + user.roles());

            switch (user.roles()) {
                case "ADMIN" ->
                        System.out.println("Access: Administration");
                case "CASHIER" ->
                        System.out.println("Access: Ticket Sales");
                case "CUSTOMER" ->
                        System.out.println("Access: Customer Services");
                default ->
                        System.out.println("Access: Standard");
            }

            System.out.println("1. View Account Information");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("\n--- ACCOUNT INFORMATION ---");
                    System.out.println("ID: " + user.id());
                    System.out.println("Username: " + user.username());
                    System.out.println("Role: " + user.roles());
                    System.out.println("Status: " + user.status());
                }
                case "2" -> {
                    System.out.println("Logout successful!");
                    loggedIn = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
