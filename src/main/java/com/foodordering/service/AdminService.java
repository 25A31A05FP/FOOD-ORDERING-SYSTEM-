package com.foodordering.service;

import com.foodordering.dao.OrderDao;
import com.foodordering.dao.UserDao;
import com.foodordering.model.Customer;
import com.foodordering.model.DashboardStats;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import com.foodordering.util.Validation;
import java.sql.SQLException;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class AdminService {
    private final UserDao userDao;
    private final OrderDao orderDao;

    public AdminService(UserDao userDao, OrderDao orderDao) {
        this.userDao = userDao;
        this.orderDao = orderDao;
    }

    public void ensureConfiguredAdmin() throws SQLException {
        if (userDao.countAdmins() > 0) {
            return;
        }
        String email = System.getenv("FOOD_ADMIN_EMAIL");
        String password = System.getenv("FOOD_ADMIN_PASSWORD");
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            System.out.println("No administrator exists. Set FOOD_ADMIN_EMAIL and FOOD_ADMIN_PASSWORD "
                    + "before starting the app to create one.");
            return;
        }

        String cleanEmail = Validation.email(email);
        String cleanPassword = Validation.password(password.toCharArray());
        userDao.createAdmin("Restaurant Administrator", cleanEmail,
                BCrypt.hashpw(cleanPassword, BCrypt.gensalt(12)));
        System.out.println("Created the configured administrator account: " + cleanEmail);
    }

    public DashboardStats stats(User actor) throws SQLException {
        requireAdmin(actor);
        return orderDao.dashboardStats();
    }

    public List<Customer> customers(User actor) throws SQLException {
        requireAdmin(actor);
        return userDao.findCustomers();
    }

    public void setCustomerActive(User actor, long customerId, boolean active) throws SQLException {
        requireAdmin(actor);
        if (customerId < 1) {
            throw new IllegalArgumentException("Select a valid customer.");
        }
        userDao.setCustomerActive(customerId, active);
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.role() != Role.ADMIN) {
            throw new SecurityException("Administrator access is required.");
        }
    }
}
