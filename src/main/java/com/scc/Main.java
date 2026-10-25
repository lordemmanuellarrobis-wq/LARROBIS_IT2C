
package com.scc;

import dao.DatabaseInitializer;
import dao.UserDao;
import ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseInitializer initializer =
                    new DatabaseInitializer();

            initializer.initialize();

            System.out.println(
                    "SQLite database initialized successfully!"
            );

            UserDao userDao = new UserDao();

            try (Scanner scanner = new Scanner(System.in)) {
                Authentication authentication =
                        new Authentication(userDao, scanner);

                authentication.start();
            }

        } catch (SQLException e) {
            System.err.println("DATABASE / APPLICATION ERROR:");
            e.printStackTrace();
        }
    }
}
