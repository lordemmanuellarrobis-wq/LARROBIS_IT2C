
package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:terminalgo.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL
                        CHECK (roles IN ('ADMIN', 'CASHIER', 'CUSTOMER')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """);

            migrateRolesIfNeeded(connection);
        }
    }

    private static void migrateRolesIfNeeded(Connection connection)
            throws SQLException {

        String tableSql = "";

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT sql FROM sqlite_master " +
                             "WHERE type = 'table' AND name = 'users'")) {

            if (result.next()) {
                tableSql = result.getString("sql");
            }
        }

        String normalized = tableSql == null
                ? ""
                : tableSql.toUpperCase();

        boolean supportsAllRoles =
                normalized.contains("ADMIN")
                        && normalized.contains("CASHIER")
                        && normalized.contains("CUSTOMER");

        if (supportsAllRoles) {
            return;
        }

        connection.setAutoCommit(false);

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE users_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL
                        CHECK (roles IN ('ADMIN', 'CASHIER', 'CUSTOMER')),
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """);

            statement.execute("""
                INSERT INTO users_new (id, username, password, roles, status)
                SELECT id, username, password, roles, status
                FROM users
                WHERE UPPER(roles) IN ('ADMIN', 'CASHIER', 'CUSTOMER')
                """);

            statement.execute("DROP TABLE users");
            statement.execute("ALTER TABLE users_new RENAME TO users");

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
