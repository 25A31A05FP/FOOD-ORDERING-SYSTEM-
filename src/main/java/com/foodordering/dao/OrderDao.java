package com.foodordering.dao;

import com.foodordering.model.DashboardStats;
import com.foodordering.model.OrderLine;
import com.foodordering.model.OrderReceipt;
import com.foodordering.model.OrderStatus;
import com.foodordering.model.OrderSummary;
import com.foodordering.model.PaymentMethod;
import com.foodordering.util.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    private final CartDao cartDao = new CartDao();

    public OrderReceipt placeOrder(long userId, PaymentMethod method) throws SQLException {
        String cartSql = """
                SELECT f.id, f.name, f.price, cart.quantity
                FROM cart_items cart
                JOIN food_items f ON f.id = cart.food_item_id
                WHERE cart.user_id = ? AND f.active = TRUE
                FOR UPDATE
                """;
        String orderSql = "INSERT INTO orders (user_id, status, total) VALUES (?, 'PENDING', ?)";
        String lineSql = """
                INSERT INTO order_items (order_id, food_item_id, item_name, unit_price, quantity, line_total)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<OrderLine> items = new ArrayList<>();
                List<Long> foodIds = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;
                try (PreparedStatement statement = connection.prepareStatement(cartSql)) {
                    statement.setLong(1, userId);
                    try (ResultSet results = statement.executeQuery()) {
                        while (results.next()) {
                            int quantity = results.getInt("quantity");
                            BigDecimal price = results.getBigDecimal("price");
                            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));
                            total = total.add(lineTotal);
                            foodIds.add(results.getLong("id"));
                            items.add(new OrderLine(results.getString("name"), price, quantity, lineTotal));
                        }
                    }
                }
                if (items.isEmpty()) {
                    throw new SQLException("Your cart is empty or contains unavailable items.");
                }

                long orderId;
                try (PreparedStatement statement =
                             connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setLong(1, userId);
                    statement.setBigDecimal(2, total);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("The database did not return an order id.");
                        }
                        orderId = keys.getLong(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(lineSql)) {
                    for (int i = 0; i < items.size(); i++) {
                        OrderLine item = items.get(i);
                        statement.setLong(1, orderId);
                        statement.setLong(2, foodIds.get(i));
                        statement.setString(3, item.itemName());
                        statement.setBigDecimal(4, item.unitPrice());
                        statement.setInt(5, item.quantity());
                        statement.setBigDecimal(6, item.lineTotal());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                String paymentSql = """
                        INSERT INTO payments (order_id, method, status, amount)
                        VALUES (?, ?, ?, ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(paymentSql)) {
                    statement.setLong(1, orderId);
                    statement.setString(2, method.name());
                    statement.setString(3, method == PaymentMethod.DEMO_UPI ? "DEMO_ONLY" : "PENDING");
                    statement.setBigDecimal(4, total);
                    statement.executeUpdate();
                }
                cartDao.clear(connection, userId);
                connection.commit();
                OrderSummary summary = new OrderSummary(orderId, userId, "", OrderStatus.PENDING,
                        total, method, java.time.LocalDateTime.now());
                return new OrderReceipt(summary, items);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<OrderSummary> findByCustomer(long userId) throws SQLException {
        String sql = """
                SELECT o.id, o.user_id, u.full_name, o.status, o.total, p.method, o.created_at
                FROM orders o
                JOIN users u ON u.id = o.user_id
                JOIN payments p ON p.order_id = o.id
                WHERE o.user_id = ?
                ORDER BY o.created_at DESC
                """;
        return findOrders(sql, userId);
    }

    public List<OrderSummary> findAll() throws SQLException {
        String sql = """
                SELECT o.id, o.user_id, u.full_name, o.status, o.total, p.method, o.created_at
                FROM orders o
                JOIN users u ON u.id = o.user_id
                JOIN payments p ON p.order_id = o.id
                ORDER BY o.created_at DESC
                """;
        return findOrders(sql);
    }

    public void updateStatus(long orderId, OrderStatus status) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE orders SET status = ? WHERE id = ?")) {
            statement.setString(1, status.name());
            statement.setLong(2, orderId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Order was not found.");
            }
        }
    }

    public DashboardStats dashboardStats() throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM orders) AS total_orders,
                    (SELECT COALESCE(SUM(total), 0) FROM orders WHERE status <> 'CANCELLED') AS revenue,
                    (SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER') AS total_customers
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            results.next();
            return new DashboardStats(results.getLong("total_orders"),
                    results.getBigDecimal("revenue"), results.getLong("total_customers"));
        }
    }

    private List<OrderSummary> findOrders(String sql, long... parameters) throws SQLException {
        List<OrderSummary> orders = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setLong(i + 1, parameters[i]);
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Timestamp createdAt = results.getTimestamp("created_at");
                    orders.add(new OrderSummary(
                            results.getLong("id"),
                            results.getLong("user_id"),
                            results.getString("full_name"),
                            OrderStatus.fromDatabase(results.getString("status")),
                            results.getBigDecimal("total"),
                            PaymentMethod.valueOf(results.getString("method")),
                            createdAt.toLocalDateTime()));
                }
            }
        }
        return List.copyOf(orders);
    }
}
