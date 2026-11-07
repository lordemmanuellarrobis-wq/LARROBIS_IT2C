package ui;

import dao.UserDao;
import model.User;
import model.UserRoleStatus;

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

            System.out.println("\n==============================================");
            System.out.println("                 ADMIN DASHBOARD");
            System.out.println("==============================================");
            System.out.println("Logged in as: " + admin.username());
            System.out.println("----------------------------------------------");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Users");
            System.out.println("3. Approve User");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role and Status");
            System.out.println("6. Delete User");
            System.out.println("7. Logout");
            System.out.println("----------------------------------------------");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {

                switch (choice) {

                    case "1" -> viewAllUsers();

                    case "2" -> viewPendingUsers();

                    case "3" -> approveUser();

                    case "4" -> addNewUser();

                    case "5" -> updateUserRoleAndStatus();

                    case "6" -> deleteUser();

                    case "7" -> {
                        System.out.println("Admin logged out.");
                        loggedIn = false;
                    }

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
    }

    // VIEW ALL USERS
    private void viewAllUsers() throws SQLException {

        System.out.println("\n================ ALL USERS ================");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        displayUsers(users);
    }

    // VIEW PENDING USERS
    private void viewPendingUsers() throws SQLException {

        System.out.println("\n========== PENDING USER APPROVALS ==========");

        List<User> users = userDao.getPendingUsers();

        if (users.isEmpty()) {
            System.out.println("No pending users found.");
            return;
        }

        displayUsers(users);
    }

    // DISPLAY USERS
    private void displayUsers(List<User> users) {

        System.out.println(
                "----------------------------------------------------------------"
        );

        System.out.printf(
                "%-6s %-22s %-15s %-15s%n",
                "ID",
                "USERNAME",
                "ROLE",
                "STATUS"
        );

        System.out.println(
                "----------------------------------------------------------------"
        );

        for (User user : users) {

            System.out.printf(
                    "%-6d %-22s %-15s %-15s%n",
                    user.id(),
                    user.username(),
                    user.role(),
                    user.status()
            );
        }

        System.out.println(
                "----------------------------------------------------------------"
        );

        System.out.println("Total users: " + users.size());
    }

    // GET USER ID
    private int getUserId() {

        System.out.print("Enter User ID (0 to cancel): ");

        String input = scanner.nextLine().trim();

        try {

            int id = Integer.parseInt(input);

            if (id < 0) {
                System.out.println("Invalid ID.");
                return -1;
            }

            return id;

        } catch (NumberFormatException e) {

            System.out.println("Please enter a valid number.");
            return -1;
        }
    }

    // APPROVE USER
    private void approveUser() throws SQLException {

        System.out.println("\n========== APPROVE USER ==========");

        List<User> users = userDao.getPendingUsers();

        if (users.isEmpty()) {
            System.out.println("No pending users found.");
            return;
        }

        displayUsers(users);

        int id = getUserId();

        if (id <= 0) {
            return;
        }

        boolean approved = userDao.approveUserById(id);

        if (approved) {
            System.out.println("User approved successfully!");
        } else {
            System.out.println("User ID not found.");
        }
    }

    // ADD NEW USER
    private void addNewUser() throws SQLException {

        System.out.println("\n========== ADD NEW USER ==========");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        System.out.println("\nAVAILABLE ROLES:");

        for (UserRoleStatus.Role role :
                UserRoleStatus.Role.values()) {

            System.out.println("- " + role);
        }

        System.out.print("Enter Role: ");

        UserRoleStatus.Role role =
                UserRoleStatus.parseRole(scanner.nextLine());

        boolean added =
                userDao.registerUser(username, password, role);

        if (added) {

            System.out.println("User added successfully!");
            System.out.println("Role: " + role);
            System.out.println("Default Status: PENDING");

        } else {

            System.out.println("Username already exists.");
        }
    }

    // UPDATE USER ROLE AND STATUS TOGETHER
    private void updateUserRoleAndStatus() throws SQLException {

        System.out.println(
                "\n========== UPDATE USER ROLE AND STATUS =========="
        );

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        // SHOW ALL USERS
        displayUsers(users);

        // SELECT USER ID
        int id = getUserId();

        if (id <= 0) {
            return;
        }

        // SELECT NEW ROLE
        System.out.println("\nAVAILABLE ROLES:");

        for (UserRoleStatus.Role role :
                UserRoleStatus.Role.values()) {

            System.out.println("- " + role);
        }

        System.out.print("Enter New Role: ");

        UserRoleStatus.Role role =
                UserRoleStatus.parseRole(scanner.nextLine());

        // SELECT NEW STATUS
        System.out.println("\nAVAILABLE STATUSES:");

        for (UserRoleStatus.Status status :
                UserRoleStatus.Status.values()) {

            System.out.println("- " + status);
        }

        System.out.print("Enter New Status: ");

        UserRoleStatus.Status status =
                UserRoleStatus.parseStatus(scanner.nextLine());

        // UPDATE ROLE AND STATUS IN ONE DATABASE OPERATION
        boolean updated =
                userDao.updateUserRoleAndStatusById(
                        id,
                        role,
                        status
                );

        if (updated) {

            System.out.println("\nUser updated successfully!");
            System.out.println("New Role: " + role);
            System.out.println("New Status: " + status);

        } else {

            System.out.println("User ID not found.");
        }
    }

    // DELETE USER
    private void deleteUser() throws SQLException {

        System.out.println("\n========== DELETE USER ==========");

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        displayUsers(users);

        int id = getUserId();

        if (id <= 0) {
            return;
        }

        System.out.print(
                "Are you sure you want to delete this user? (Y/N): "
        );

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {

            System.out.println("Delete cancelled.");
            return;
        }

        boolean deleted = userDao.deleteUserById(id);

        if (deleted) {
            System.out.println("User deleted successfully!");
        } else {
            System.out.println("User ID not found.");
        }
    }
}