package com.foodordering.dao;

import com.foodordering.model.Customer;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import com.foodordering.util.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    public record LoginRecord(User user, String passwordHash) {
    }

    public Optional<LoginRecord> findLoginRecord(String email) throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, role
                FROM users WHERE email = ? AND active = TRUE
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return Optional.empty();
                }
                User user = new User(
                        results.getLong("id"),
                        results.getString("full_name"),
                        results.getString("email"),
                        Role.valueOf(results.getString("role")));
                return Optional.of(new LoginRecord(user, results.getString("password_hash")));
            }
        }
    }

    public User createCustomer(String name, String email, String passwordHash) throws SQLException {
        return insert(name, email, passwordHash, Role.CUSTOMER);
    }

    public User createAdmin(String name, String email, String passwordHash) throws SQLException {
        return insert(name, email, passwordHash, Role.ADMIN);
    }

    public long countAdmins() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'");
             ResultSet results = statement.executeQuery()) {
            results.next();
            return results.getLong(1);
        }
    }

    public List<Customer> findCustomers() throws SQLException {
        String sql = """
                SELECT id, full_name, email, active
                FROM users WHERE role = 'CUSTOMER'
                ORDER BY created_at DESC
                """;
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                customers.add(new Customer(
                        results.getLong("id"),
                        results.getString("full_name"),
                        results.getString("email"),
                        results.getBoolean("active")));
            }
        }
        return List.copyOf(customers);
    }

    public void setCustomerActive(long customerId, boolean active) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE id = ? AND role = 'CUSTOMER'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setLong(2, customerId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Customer was not found or is no longer available.");
            }
        }
    }

    private User insert(String name, String email, String passwordHash, Role role) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("The database did not return a user id.");
                }
                return new User(keys.getLong(1), name, email, role);
            }
        }
    }
}
