
package dao;

import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class UserDao {

    // REGISTER USER
    public boolean registerUser(
            String username,
            String password,
            String roles) throws SQLException {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || roles == null || roles.isBlank()) {
            throw new IllegalArgumentException(
                    "Username, password, and role are required."
            );
        }

        String normalizedUsername = username.trim();
        String normalizedRole = roles.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("ADMIN")
                && !normalizedRole.equals("CASHIER")
                && !normalizedRole.equals("CUSTOMER")) {
            throw new IllegalArgumentException("Invalid role.");
        }

        if (usernameExists(normalizedUsername)) {
            return false;
        }

        // Hash the password before saving it.
        String hashedPassword =
                BCrypt.hashpw(password, BCrypt.gensalt(12));

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, normalizedUsername);
            statement.setString(2, hashedPassword);
            statement.setString(3, normalizedRole);

            return statement.executeUpdate() > 0;
        }
    }

    // LOGIN USER
    public User login(String username, String password)
            throws SQLException {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    System.out.println("Username not found.");
                    return null;
                }

                String storedHash = result.getString("password");

                if (storedHash == null
                        || !storedHash.startsWith("$2")) {
                    System.out.println("Invalid password hash.");
                    return null;
                }

                // Check the entered password against the database hash.
                boolean passwordMatches;

                try {
                    passwordMatches = BCrypt.checkpw(
                            password,
                            result.getString("password")
                    );
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid stored password hash.");
                    return null;
                }

                if (!passwordMatches) {
                    System.out.println("Incorrect password.");
                    return null;
                }

                String status = result.getString("status");

                // Only approved accounts can log in.
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

    // CHECK IF USERNAME EXISTS
    public boolean usernameExists(String username)
            throws SQLException {

        if (username == null || username.isBlank()) {
            return false;
        }

        String sql =
                "SELECT 1 FROM users WHERE username = ?";

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
