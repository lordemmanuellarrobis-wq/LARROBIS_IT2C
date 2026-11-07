
package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public void initialize() throws SQLException {

        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL
                        CHECK (roles IN (
                            'ADMIN', 'CASHIER', 'CUSTOMER'
                        )),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN (
                            'PENDING', 'APPROVED', 'REJECTED'
                        ))
                )
                """;

        String createActivityLogsTable = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createLogIndex = """
                CREATE INDEX IF NOT EXISTS
                idx_activity_logs_user_id
                ON activity_logs(user_id)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             Statement statement =
                     connection.createStatement()) {

            statement.execute(createUsersTable);

            statement.execute(createActivityLogsTable);

            statement.execute(createLogIndex);

            System.out.println(
                    "Users table initialized."
            );

            System.out.println(
                    "Activity logs table initialized."
            );
        }
    }
}
