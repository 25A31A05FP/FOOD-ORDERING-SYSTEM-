package com.foodordering.dao;

import com.foodordering.model.CartItem;
import com.foodordering.model.FoodItem;
import com.foodordering.util.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartDao {
    public List<CartItem> findItems(long userId) throws SQLException {
        String sql = """
                SELECT f.id, f.category_id, c.name AS category, f.name,
                       f.description, f.price, f.image_path, f.active, cart.quantity
                FROM cart_items cart
                JOIN food_items f ON f.id = cart.food_item_id
                JOIN categories c ON c.id = f.category_id
                WHERE cart.user_id = ? AND f.active = TRUE
                ORDER BY f.name
                """;
        List<CartItem> items = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    FoodItem food = new FoodItem(
                            results.getLong("id"),
                            results.getLong("category_id"),
                            results.getString("category"),
                            results.getString("name"),
                            results.getString("description"),
                            results.getBigDecimal("price"),
                            results.getString("image_path"),
                            results.getBoolean("active"));
                    items.add(new CartItem(food, results.getInt("quantity")));
                }
            }
        }
        return List.copyOf(items);
    }

    public void add(long userId, long foodId, int amount) throws SQLException {
        String sql = """
                INSERT INTO cart_items (user_id, food_item_id, quantity)
                SELECT ?, id, ? FROM food_items WHERE id = ? AND active = TRUE
                ON DUPLICATE KEY UPDATE quantity = LEAST(quantity + VALUES(quantity), 99)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setInt(2, amount);
            statement.setLong(3, foodId);
            if (statement.executeUpdate() == 0) {
                try (PreparedStatement verify = connection.prepareStatement(
                        "SELECT 1 FROM food_items WHERE id = ? AND active = TRUE")) {
                    verify.setLong(1, foodId);
                    try (ResultSet result = verify.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Food item is unavailable.");
                        }
                    }
                }
            }
        }
    }

    public void setQuantity(long userId, long foodId, int quantity) throws SQLException {
        if (quantity == 0) {
            remove(userId, foodId);
            return;
        }
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND food_item_id = ?")) {
            statement.setInt(1, quantity);
            statement.setLong(2, userId);
            statement.setLong(3, foodId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Cart item was not found.");
            }
        }
    }

    public void remove(long userId, long foodId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM cart_items WHERE user_id = ? AND food_item_id = ?")) {
            statement.setLong(1, userId);
            statement.setLong(2, foodId);
            statement.executeUpdate();
        }
    }

    public void clear(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM cart_items WHERE user_id = ?")) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }
}
