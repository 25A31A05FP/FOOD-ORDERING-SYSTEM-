package com.foodordering.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/food_ordering_system?serverTimezone=UTC";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("FOOD_DB_URL", DEFAULT_URL);
        String username = System.getenv().getOrDefault("FOOD_DB_USER", "root");
        String password = System.getenv().getOrDefault("FOOD_DB_PASSWORD", "");
        return DriverManager.getConnection(url, username, password);
    }
}
