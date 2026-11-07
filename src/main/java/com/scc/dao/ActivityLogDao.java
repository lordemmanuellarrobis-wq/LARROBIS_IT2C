
package dao;

import model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // INSERT ACTIVITY LOG
    public void logAction(
            int userId,
            String actionType,
            String description
    ) throws SQLException {

        if (userId <= 0 ||
                actionType == null ||
                actionType.isBlank() ||
                description == null ||
                description.isBlank()) {

            throw new IllegalArgumentException(
                    "Valid user ID, action type, and description are required."
            );
        }

        String sql = """
                INSERT INTO activity_logs
                (user_id, action_type, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType);
            statement.setString(3, description);

            statement.executeUpdate();
        }
    }

    // GET ALL SYSTEM LOGS
    public List<ActivityLog> getAllLogs()
            throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                ORDER BY timestamp DESC, id DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result =
                     statement.executeQuery()) {

            while (result.next()) {

                logs.add(new ActivityLog(
                        result.getInt("id"),
                        result.getInt("user_id"),
                        result.getString("action_type"),
                        result.getString("description"),
                        result.getString("timestamp")
                ));
            }
        }

        return logs;
    }

    // GET LOGS BY USER ID
    public List<ActivityLog> getLogsByUserId(int userId)
            throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid User ID."
            );
        }

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT id, user_id, action_type,
                       description, timestamp
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, id DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    logs.add(new ActivityLog(
                            result.getInt("id"),
                            result.getInt("user_id"),
                            result.getString("action_type"),
                            result.getString("description"),
                            result.getString("timestamp")
                    ));
                }
            }
        }

        return logs;
    }

    // CLEAR ALL HISTORICAL LOGS
    public int clearAllLogs() throws SQLException {

        String sql = "DELETE FROM activity_logs";

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            return statement.executeUpdate();
        }
    }
}
