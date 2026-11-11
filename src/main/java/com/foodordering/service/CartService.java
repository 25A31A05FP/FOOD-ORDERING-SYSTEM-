package com.foodordering.service;

import com.foodordering.dao.CartDao;
import com.foodordering.model.CartItem;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import java.sql.SQLException;
import java.util.List;

public class CartService {
    private final CartDao cartDao;

    public CartService(CartDao cartDao) {
        this.cartDao = cartDao;
    }

    public List<CartItem> items(User customer) throws SQLException {
        requireCustomer(customer);
        return cartDao.findItems(customer.id());
    }

    public void add(User customer, long foodId) throws SQLException {
        requireCustomer(customer);
        if (foodId < 1) {
            throw new IllegalArgumentException("Select a valid food item.");
        }
        cartDao.add(customer.id(), foodId, 1);
    }

    public void setQuantity(User customer, long foodId, int quantity) throws SQLException {
        requireCustomer(customer);
        if (foodId < 1 || quantity < 0 || quantity > 99) {
            throw new IllegalArgumentException("Choose a quantity between 0 and 99.");
        }
        cartDao.setQuantity(customer.id(), foodId, quantity);
    }

    public void remove(User customer, long foodId) throws SQLException {
        requireCustomer(customer);
        if (foodId < 1) {
            throw new IllegalArgumentException("Select a valid cart item.");
        }
        cartDao.remove(customer.id(), foodId);
    }

    private void requireCustomer(User customer) {
        if (customer == null || customer.role() != Role.CUSTOMER) {
            throw new SecurityException("Customer access is required.");
        }
    }
}
