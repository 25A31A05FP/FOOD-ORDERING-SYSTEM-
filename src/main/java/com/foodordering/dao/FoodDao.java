package com.foodordering.dao;

import com.foodordering.model.Category;
import com.foodordering.model.FoodItem;
import com.foodordering.util.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FoodDao {
    private static final String SELECT_COLUMNS = """
            SELECT f.id, f.category_id, c.name AS category, f.name,
                   f.description, f.price, f.image_path, f.active
            FROM food_items f JOIN categories c ON c.id = f.category_id
            """;

    public List<Category> findCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM categories ORDER BY id");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                categories.add(new Category(results.getLong("id"), results.getString("name")));
            }
        }
        return List.copyOf(categories);
    }

    public List<FoodItem> findAvailable(String search, String category) throws SQLException {
        String sql = SELECT_COLUMNS + """
                WHERE f.active = TRUE
                  AND (? = '' OR LOWER(f.name) LIKE ? OR LOWER(f.description) LIKE ?)
                  AND (? = '' OR c.name = ?)
                ORDER BY c.id, f.name
                """;
        return query(sql, search.strip().toLowerCase(), "%" + search.strip().toLowerCase() + "%",
                "%" + search.strip().toLowerCase() + "%", category, category);
    }

    public List<FoodItem> findAllForAdmin() throws SQLException {
        return query(SELECT_COLUMNS + " ORDER BY c.id, f.name");
    }

    public void save(FoodItem item) throws SQLException {
        if (item.id() < 1) {
            String sql = """
                    INSERT INTO food_items (category_id, name, description, price, image_path, active)
                    VALUES (?, ?, ?, ?, ?, TRUE)
                    """;
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(statement, item);
                statement.executeUpdate();
            }
            return;
        }

        String sql = """
                UPDATE food_items
                SET category_id = ?, name = ?, description = ?, price = ?, image_path = ?
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, item);
            statement.setLong(6, item.id());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Food item was not found.");
            }
        }
    }

    public void setActive(long foodId, boolean active) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE food_items SET active = ? WHERE id = ?")) {
                    statement.setBoolean(1, active);
                    statement.setLong(2, foodId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Food item was not found.");
                    }
                }
                if (!active) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM cart_items WHERE food_item_id = ?")) {
                        statement.setLong(1, foodId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private List<FoodItem> query(String sql, String... parameters) throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setString(i + 1, parameters[i]);
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    items.add(new FoodItem(
                            results.getLong("id"),
                            results.getLong("category_id"),
                            results.getString("category"),
                            results.getString("name"),
                            results.getString("description"),
                            results.getBigDecimal("price"),
                            results.getString("image_path"),
                            results.getBoolean("active")));
                }
            }
        }
        return List.copyOf(items);
    }

    private void bind(PreparedStatement statement, FoodItem item) throws SQLException {
        statement.setLong(1, item.categoryId());
        statement.setString(2, item.name());
        statement.setString(3, item.description());
        statement.setBigDecimal(4, item.price());
        statement.setString(5, item.imagePath());
    }
}
