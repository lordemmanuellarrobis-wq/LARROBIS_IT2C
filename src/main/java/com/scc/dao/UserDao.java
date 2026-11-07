package dao;

import model.User;
import model.UserRoleStatus;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // REGISTER USER
    public boolean registerUser(
            String username,
            String password,
            UserRoleStatus.Role role
    ) throws SQLException {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || role == null) {

            throw new IllegalArgumentException(
                    "Username, password, and role are required."
            );
        }

        if (usernameExists(username.trim())) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());
            statement.setString(2, hashedPassword);
            statement.setString(3, role.name());
            statement.setString(
                    4,
                    UserRoleStatus.Status.PENDING.name()
            );

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
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    System.out.println("Username not found.");
                    return null;
                }

                String storedPassword = result.getString("password");
                boolean passwordMatches = false;

                if (storedPassword != null
                        && storedPassword.startsWith("$2")) {

                    try {
                        passwordMatches =
                                BCrypt.checkpw(password, storedPassword);
                    } catch (IllegalArgumentException e) {
                        passwordMatches = false;
                    }

                } else if (storedPassword != null) {

                    // Compatibility with older plain-text passwords.
                    passwordMatches = password.equals(storedPassword);
                }

                if (!passwordMatches) {
                    System.out.println("Incorrect password.");
                    return null;
                }

                UserRoleStatus.Role role =
                        UserRoleStatus.parseRole(
                                result.getString("roles")
                        );

                UserRoleStatus.Status status =
                        UserRoleStatus.parseStatus(
                                result.getString("status")
                        );

                if (status != UserRoleStatus.Status.APPROVED) {

                    if (status == UserRoleStatus.Status.REJECTED) {
                        System.out.println(
                                "Your account was rejected. Contact the administrator."
                        );
                    } else {
                        System.out.println(
                                "Your account is pending approval."
                        );
                    }

                    return null;
                }

                return new User(
                        result.getInt("id"),
                        result.getString("username"),
                        storedPassword,
                        role,
                        status
                );
            }
        }
    }

    // CHECK IF USERNAME EXISTS
    public boolean usernameExists(String username) throws SQLException {

        String sql = """
                SELECT 1
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // VIEW ALL USERS
    public List<User> getAllUsers() throws SQLException {
        return getUsers(false);
    }

    // VIEW PENDING USERS
    public List<User> getPendingUsers() throws SQLException {
        return getUsers(true);
    }

    // GET USERS FROM DATABASE
    private List<User> getUsers(boolean pendingOnly) throws SQLException {

        List<User> users = new ArrayList<>();

        String sql;

        if (pendingOnly) {
            sql = """
                    SELECT id, username, password, roles, status
                    FROM users
                    WHERE UPPER(status) = 'PENDING'
                    ORDER BY id
                    """;
        } else {
            sql = """
                    SELECT id, username, password, roles, status
                    FROM users
                    ORDER BY id
                    """;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                User user = new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        UserRoleStatus.parseRole(
                                result.getString("roles")
                        ),
                        UserRoleStatus.parseStatus(
                                result.getString("status")
                        )
                );

                users.add(user);
            }
        }

        return users;
    }

    // APPROVE USER
    public boolean approveUserById(int userId) throws SQLException {

        return updateUserStatusById(
                userId,
                UserRoleStatus.Status.APPROVED
        );
    }

    // UPDATE USER ROLE AND STATUS TOGETHER
    public boolean updateUserRoleAndStatusById(
            int userId,
            UserRoleStatus.Role role,
            UserRoleStatus.Status status
    ) throws SQLException {

        if (userId <= 0 || role == null || status == null) {

            throw new IllegalArgumentException(
                    "Valid user ID, role, and status are required."
            );
        }

        String sql = """
                UPDATE users
                SET roles = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role.name());
            statement.setString(2, status.name());
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;
        }
    }

    // UPDATE STATUS ONLY
    // Used by the Approve User option.
    public boolean updateUserStatusById(
            int userId,
            UserRoleStatus.Status status
    ) throws SQLException {

        if (userId <= 0 || status == null) {

            throw new IllegalArgumentException(
                    "Valid user ID and status are required."
            );
        }

        String sql = """
                UPDATE users
                SET status = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    // DELETE USER
    public boolean deleteUserById(int userId) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be positive."
            );
        }

        String sql = """
                DELETE FROM users
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            return statement.executeUpdate() > 0;
        }
    }
}