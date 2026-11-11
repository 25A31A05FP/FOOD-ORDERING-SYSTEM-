package com.foodordering.controller;

import com.foodordering.model.CartItem;
import com.foodordering.model.Category;
import com.foodordering.model.Customer;
import com.foodordering.model.DashboardStats;
import com.foodordering.model.FoodItem;
import com.foodordering.model.OrderReceipt;
import com.foodordering.model.OrderStatus;
import com.foodordering.model.OrderSummary;
import com.foodordering.model.PaymentMethod;
import com.foodordering.model.User;
import com.foodordering.service.AdminService;
import com.foodordering.service.AuthService;
import com.foodordering.service.CartService;
import com.foodordering.service.FoodService;
import com.foodordering.service.OrderService;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AppController {
    private final AuthService auth;
    private final FoodService food;
    private final CartService cart;
    private final OrderService orders;
    private final AdminService admin;

    public AppController(AuthService auth, FoodService food, CartService cart,
                         OrderService orders, AdminService admin) {
        this.auth = auth;
        this.food = food;
        this.cart = cart;
        this.orders = orders;
        this.admin = admin;
    }

    public Optional<User> login(String email, char[] password) throws SQLException {
        return auth.login(email, password);
    }

    public User register(String name, String email, char[] password) throws SQLException {
        return auth.registerCustomer(name, email, password);
    }

    public List<Category> categories() throws SQLException {
        return food.categories();
    }

    public List<FoodItem> menu(String search, String category) throws SQLException {
        return food.menu(search, category);
    }

    public List<FoodItem> adminMenu(User actor) throws SQLException {
        return food.allForAdmin(actor);
    }

    public void saveFood(User actor, FoodItem item) throws SQLException {
        food.save(actor, item);
    }

    public void setFoodActive(User actor, long id, boolean active) throws SQLException {
        food.setActive(actor, id, active);
    }

    public List<CartItem> cart(User customer) throws SQLException {
        return cart.items(customer);
    }

    public void addToCart(User customer, long foodId) throws SQLException {
        cart.add(customer, foodId);
    }

    public void setCartQuantity(User customer, long foodId, int quantity) throws SQLException {
        cart.setQuantity(customer, foodId, quantity);
    }

    public void removeFromCart(User customer, long foodId) throws SQLException {
        cart.remove(customer, foodId);
    }

    public OrderReceipt checkout(User customer, PaymentMethod method) throws SQLException {
        return orders.placeOrder(customer, method);
    }

    public List<OrderSummary> orderHistory(User customer) throws SQLException {
        return orders.history(customer);
    }

    public List<OrderSummary> allOrders(User actor) throws SQLException {
        return orders.allOrders(actor);
    }

    public void updateOrderStatus(User actor, long orderId, OrderStatus status) throws SQLException {
        orders.updateStatus(actor, orderId, status);
    }

    public DashboardStats dashboardStats(User actor) throws SQLException {
        return admin.stats(actor);
    }

    public List<Customer> customers(User actor) throws SQLException {
        return admin.customers(actor);
    }

    public void setCustomerActive(User actor, long id, boolean active) throws SQLException {
        admin.setCustomerActive(actor, id, active);
    }
}
