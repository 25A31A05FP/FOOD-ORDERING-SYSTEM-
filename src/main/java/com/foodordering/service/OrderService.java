package com.foodordering.service;

import com.foodordering.dao.OrderDao;
import com.foodordering.model.OrderReceipt;
import com.foodordering.model.OrderStatus;
import com.foodordering.model.OrderSummary;
import com.foodordering.model.PaymentMethod;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import java.sql.SQLException;
import java.util.List;

public class OrderService {
    private final OrderDao orderDao;

    public OrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public OrderReceipt placeOrder(User customer, PaymentMethod method) throws SQLException {
        requireCustomer(customer);
        if (method == null) {
            throw new IllegalArgumentException("Choose a payment method.");
        }
        return orderDao.placeOrder(customer.id(), method);
    }

    public List<OrderSummary> history(User customer) throws SQLException {
        requireCustomer(customer);
        return orderDao.findByCustomer(customer.id());
    }

    public List<OrderSummary> allOrders(User actor) throws SQLException {
        requireAdmin(actor);
        return orderDao.findAll();
    }

    public void updateStatus(User actor, long orderId, OrderStatus status) throws SQLException {
        requireAdmin(actor);
        if (orderId < 1 || status == null) {
            throw new IllegalArgumentException("Choose a valid order and status.");
        }
        orderDao.updateStatus(orderId, status);
    }

    private void requireCustomer(User customer) {
        if (customer == null || customer.role() != Role.CUSTOMER) {
            throw new SecurityException("Customer access is required.");
        }
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.role() != Role.ADMIN) {
            throw new SecurityException("Administrator access is required.");
        }
    }
}
