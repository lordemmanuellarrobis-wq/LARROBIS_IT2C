
package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    // ADMIN DASHBOARD
    public void display(User admin) {

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println("\n==================================");
            System.out.println("          ADMIN DASHBOARD");
            System.out.println("==================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> viewAllUsers();
                    case "2" -> viewPendingUsers();
                    case "3" -> approveUser();
                    case "4" -> addNewUser();

                    case "5" -> System.out.println(
                            "Update functionality not implemented yet."
                    );

                    case "6" -> System.out.println(
                            "Delete functionality not implemented yet."
                    );

                    case "7" -> {
                        System.out.println("Admin logged out.");
                        loggedIn = false;
                    }

                    default -> System.out.println(
                            "Invalid choice. Please choose 1-7."
                    );
                }

            } catch (SQLException e) {
                System.err.println(
                        "Database error: " + e.getMessage()
                );
            }
        }
    }

    // OPTION 1: VIEW ALL USERS
    private void viewAllUsers() throws SQLException {

        System.out.println("\n============== ALL USERS ==============");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        printUsers(users);

        System.out.println("Total users: " + users.size());
    }

    // OPTION 2: VIEW PENDING USERS
    private void viewPendingUsers() throws SQLException {

        System.out.println("\n======= PENDING USER APPROVALS =======");

        List<User> users = userDao.getPendingUsers();

        if (users.isEmpty()) {
            System.out.println("No pending users found.");
            return;
        }

        printUsers(users);

        System.out.println("Pending users: " + users.size());
    }

    // DISPLAY USER TABLE
    private void printUsers(List<User> users) {

        System.out.printf(
                "%-6s %-20s %-12s %-12s%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );

        System.out.println("------------------------------------------------");

        for (User user : users) {
            System.out.printf(
                    "%-6d %-20s %-12s %-12s%n",
                    user.id(),
                    user.username(),
                    user.roles(),
                    user.status()
            );
        }

        System.out.println("------------------------------------------------");
    }

    // OPTION 3: APPROVE USER REGISTRATION
    private void approveUser() throws SQLException {

        System.out.println("\n====== APPROVE USER REGISTRATION ======");

        List<User> users = userDao.getPendingUsers();

        if (users.isEmpty()) {
            System.out.println("No pending users to approve.");
            return;
        }

        printUsers(users);

        System.out.print(
                "Enter User ID to approve (0 to cancel): "
        );

        String input = scanner.nextLine().trim();

        try {
            int userId = Integer.parseInt(input);

            if (userId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            if (userId < 0) {
                System.out.println("User ID must be positive.");
                return;
            }

            boolean approved = userDao.approveUserById(userId);

            if (approved) {
                System.out.println(
                        "User ID " + userId
                                + " approved successfully!"
                );
            } else {
                System.out.println(
                        "User ID not found or account is not pending."
                );
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID. Please enter a number.");
        }
    }

    // OPTION 4: ADD NEW USER
    private void addNewUser() throws SQLException {

        System.out.println("\n========== ADD NEW USER ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Role (ADMIN/CASHIER/CUSTOMER): ");
        String role = scanner.nextLine().trim();

        try {
            boolean registered = userDao.registerUser(
                    username,
                    password,
                    role
            );

            if (registered) {
                System.out.println(
                        "User registered successfully. Status: PENDING"
                );
            } else {
                System.out.println("Username already exists.");
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
