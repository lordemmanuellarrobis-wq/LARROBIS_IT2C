package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL
                        CHECK (
                            roles IN ('ADMIN', 'CASHIER', 'CUSTOMER')
                        ),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """);
        }

        // Keep existing role-migration logic here or invoke a
        // suitable public migration method if legacy databases exist.
    }
}