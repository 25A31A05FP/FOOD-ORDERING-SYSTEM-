package com.foodordering;

import com.foodordering.controller.AppController;
import com.foodordering.dao.CartDao;
import com.foodordering.dao.FoodDao;
import com.foodordering.dao.OrderDao;
import com.foodordering.dao.UserDao;
import com.foodordering.service.AdminService;
import com.foodordering.service.AuthService;
import com.foodordering.service.CartService;
import com.foodordering.service.FoodService;
import com.foodordering.service.OrderService;
import com.foodordering.util.Database;
import com.foodordering.view.LoginFrame;
import com.foodordering.view.Theme;
import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        Theme.install();
        try {
            UserDao userDao = new UserDao();
            FoodDao foodDao = new FoodDao();
            CartDao cartDao = new CartDao();
            OrderDao orderDao = new OrderDao();
            AdminService adminService = new AdminService(userDao, orderDao);
            try (Connection ignored = Database.getConnection()) {
                adminService.ensureConfiguredAdmin();
            }

            AppController controller = new AppController(
                    new AuthService(userDao),
                    new FoodService(foodDao),
                    new CartService(cartDao),
                    new OrderService(orderDao),
                    adminService);
            LoginFrame login = new LoginFrame(controller, user -> {
                new com.foodordering.view.MainFrame(controller, user).setVisible(true);
            });
            login.setVisible(true);
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            String message = "Could not start the application. Confirm MySQL is running, execute "
                    + "database/schema.sql and database/sample_data.sql, and set FOOD_DB_USER and "
                    + "FOOD_DB_PASSWORD if needed.\n\n" + exception.getMessage();
            JOptionPane.showMessageDialog(null, message, "Startup failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
