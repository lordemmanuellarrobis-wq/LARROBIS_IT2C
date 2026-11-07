
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import model.User;
import model.ActivityLog;
import model.UserRoleStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;

    private User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.activityLogDao = new ActivityLogDao();
        this.scanner = scanner;
    }

    // ADMIN DASHBOARD
    public void display(User admin) {

        currentAdmin = admin;

        boolean loggedIn = true;

        while (loggedIn) {

            System.out.println("\n==============================================");
            System.out.println("                 ADMIN DASHBOARD");
            System.out.println("==============================================");
            System.out.println("Logged in as: " + admin.username());
            System.out.println("Admin ID: " + admin.id());
            System.out.println("----------------------------------------------");

            System.out.println("1. View All Users");
            System.out.println("2. View Pending Users");
            System.out.println("3. Approve User");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role and Status");
            System.out.println("6. Delete User");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");

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

                    case "7" -> auditLogMenu();

                    case "8" -> {
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

        currentAdmin = null;
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
                "ID", "USERNAME", "ROLE", "STATUS"
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

        boolean approved = userDao.approveUserById(
                id,
                currentAdmin.id()
        );

        if (approved) {

            System.out.println("User approved successfully!");
            System.out.println("Audit log recorded.");

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
                UserRoleStatus.parseRole(
                        scanner.nextLine()
                );

        boolean added = userDao.registerUser(
                username,
                password,
                role
        );

        if (added) {

            System.out.println("User added successfully!");
            System.out.println("Role: " + role);
            System.out.println("Default Status: PENDING");
            System.out.println("Registration log recorded.");

        } else {

            System.out.println("Username already exists.");
        }
    }

    // UPDATE ROLE AND STATUS
    private void updateUserRoleAndStatus()
            throws SQLException {

        System.out.println(
                "\n========== UPDATE USER ROLE AND STATUS =========="
        );

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

        System.out.println("\nAVAILABLE ROLES:");

        for (UserRoleStatus.Role role :
                UserRoleStatus.Role.values()) {

            System.out.println("- " + role);
        }

        System.out.print("Enter New Role: ");

        UserRoleStatus.Role role =
                UserRoleStatus.parseRole(
                        scanner.nextLine()
                );

        System.out.println("\nAVAILABLE STATUSES:");

        for (UserRoleStatus.Status status :
                UserRoleStatus.Status.values()) {

            System.out.println("- " + status);
        }

        System.out.print("Enter New Status: ");

        UserRoleStatus.Status status =
                UserRoleStatus.parseStatus(
                        scanner.nextLine()
                );

        boolean updated =
                userDao.updateUserRoleAndStatusById(
                        id,
                        role,
                        status,
                        currentAdmin.id()
                );

        if (updated) {

            System.out.println("\nUser updated successfully!");
            System.out.println("New Role: " + role);
            System.out.println("New Status: " + status);
            System.out.println("Audit log recorded.");

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

        if (id == currentAdmin.id()) {
            System.out.println(
                    "You cannot delete your own admin account."
            );
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

        boolean deleted = userDao.deleteUserById(
                id,
                currentAdmin.id()
        );

        if (deleted) {

            System.out.println("User deleted successfully!");
            System.out.println("Audit log recorded.");

        } else {

            System.out.println("User ID not found.");
        }
    }

    // ==========================================
    // AUDIT LOG MENU
    // ==========================================

    private void auditLogMenu() throws SQLException {

        boolean running = true;

        while (running) {

            System.out.println("\n==============================================");
            System.out.println("              SYSTEM AUDIT LOGS");
            System.out.println("==============================================");

            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back to Admin Dashboard");

            System.out.println("----------------------------------------------");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewAllLogs();

                case "2" -> filterLogsByUserId();

                case "3" -> clearHistoricalLogs();

                case "4" -> running = false;

                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // VIEW ALL LOGS
    private void viewAllLogs() throws SQLException {

        System.out.println("\n========== ALL SYSTEM AUDIT LOGS ==========");

        List<ActivityLog> logs =
                activityLogDao.getAllLogs();

        displayLogs(logs);
    }

    // FILTER LOGS BY USER ID
    private void filterLogsByUserId()
            throws SQLException {

        System.out.println("\n========== FILTER LOGS BY USER ID ==========");

        int userId = getUserId();

        if (userId <= 0) {
            return;
        }

        List<ActivityLog> logs =
                activityLogDao.getLogsByUserId(userId);

        System.out.println("Showing logs for User ID: " + userId);

        displayLogs(logs);
    }

    // DISPLAY AUDIT LOGS
    private void displayLogs(List<ActivityLog> logs) {

        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        System.out.println(
                "------------------------------------------------------------------------------------------------------------------------"
        );

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(
                "------------------------------------------------------------------------------------------------------------------------"
        );

        for (ActivityLog log : logs) {

            String description = log.description();

            if (description.length() > 35) {
                description = description.substring(0, 32) + "...";
            }

            System.out.printf(
                    "%-5d | %-8d | %-18s | %-35s | %-20s%n",
                    log.id(),
                    log.userId(),
                    log.actionType(),
                    description,
                    log.timestamp()
            );
        }

        System.out.println(
                "------------------------------------------------------------------------------------------------------------------------"
        );

        System.out.println("Total logs: " + logs.size());
    }

    // CLEAR HISTORICAL LOGS
    private void clearHistoricalLogs() throws SQLException {

        System.out.println("\n========== CLEAR HISTORICAL LOGS ==========");

        System.out.println(
                "WARNING: This will permanently delete all audit logs."
        );

        System.out.print(
                "Are you sure? Type YES to continue: "
        );

        String confirmation =
                scanner.nextLine().trim();

        if (!confirmation.equals("YES")) {

            System.out.println("Clear operation cancelled.");
            return;
        }

        int deleted = activityLogDao.clearAllLogs();

        System.out.println(
                deleted + " historical logs deleted."
        );

        // Do not log this operation into the table being cleared.
    }
}
