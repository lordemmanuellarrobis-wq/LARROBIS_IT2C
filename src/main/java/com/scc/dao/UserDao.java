
package dao;

import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    // REGISTER USER
    public boolean registerUser(
            String username,
            String password,
            String role
    ) throws SQLException {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Username, password, and role are required."
            );
        }

        String normalizedUsername = username.trim();
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("ADMIN")
                && !normalizedRole.equals("CASHIER")
                && !normalizedRole.equals("CUSTOMER")) {
            throw new IllegalArgumentException(
                    "Choose ADMIN, CASHIER, or CUSTOMER."
            );
        }

        if (usernameExists(normalizedUsername)) {
            return false;
        }

        // Hash password before saving it.
        String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, normalizedUsername);
            statement.setString(2, hashedPassword);
            statement.setString(3, normalizedRole);

            return statement.executeUpdate() > 0;
        }
    }

    // LOGIN USER
    public User login(
            String username,
            String password
    ) throws SQLException {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            System.out.println("Username and password are required.");
            return null;
        }

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    System.out.println("Username not found.");
                    return null;
                }

                String storedHash = result.getString("password");
                boolean passwordMatches = false;

                if (storedHash != null && storedHash.startsWith("$2")) {
                    try {
                        passwordMatches =
                                BCrypt.checkpw(password, storedHash);
                    } catch (IllegalArgumentException e) {
                        passwordMatches = false;
                    }
                } else if (storedHash != null) {
                    // Supports older plain-text records temporarily.
                    // New registrations always use BCrypt.
                    passwordMatches = password.equals(storedHash);
                }

                if (!passwordMatches) {
                    System.out.println("Incorrect password.");
                    return null;
                }

                String status = result.getString("status");

                if (status == null
                        || !status.equalsIgnoreCase("APPROVED")) {
                    System.out.println(
                            "Your account is pending approval."
                    );
                    return null;
                }

                return new User(
                        result.getInt("id"),
                        result.getString("username"),
                        storedHash,
                        result.getString("roles"),
                        status
                );
            }
        }
    }

    // CHECK WHETHER USERNAME EXISTS
    public boolean usernameExists(String username) throws SQLException {

        if (username == null || username.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // VIEW ALL USERS
    public List<User> getAllUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    // VIEW PENDING USERS
    public List<User> getPendingUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE UPPER(status) = 'PENDING'
                ORDER BY id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }
        }

        return users;
    }

    // APPROVE USER BY ID
    public boolean approveUserById(int userId) throws SQLException {

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ?
                  AND UPPER(status) = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;
        }
    }
}
