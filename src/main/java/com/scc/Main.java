package com.scc;

import dao.DatabaseInitializer;
import dao.UserDao;
import ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {

            // INITIALIZE DATABASE FIRST
            DatabaseInitializer initializer =
                    new DatabaseInitializer();

            initializer.initialize();

            System.out.println(
                    "Database initialized successfully!"
            );

            // CREATE USER DAO
            UserDao userDao = new UserDao();

            // START AUTHENTICATION
            try (Scanner scanner = new Scanner(System.in)) {

                Authentication authentication =
                        new Authentication(userDao, scanner);

                authentication.start();
            }

        } catch (SQLException e) {

            System.out.println(
                    "Application database error:"
            );

            e.printStackTrace();
        }
    }
}