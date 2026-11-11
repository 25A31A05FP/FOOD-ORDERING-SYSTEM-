package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame {
    private final AppController controller;
    private final User user;
    private final JPanel content = new JPanel(new CardLayout());
    private final JLabel pageTitle = new JLabel();
    private final JLabel cartCount = new JLabel("0 items");

    public MainFrame(AppController controller, User user) {
        this.controller = controller;
        this.user = user;
        setTitle("Table & Thyme");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(createWorkspace(), BorderLayout.CENTER);
        openPage(user.role() == Role.ADMIN ? "Overview" : "Menu");
        refreshCartCount();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.GREEN_DARK);
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(23, 16, 20, 16));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("TABLE & THYME");
        brand.setFont(Theme.font(14, Font.BOLD));
        brand.setForeground(new Color(210, 230, 210));
        brand.setBorder(BorderFactory.createEmptyBorder(3, 10, 28, 4));
        sidebar.add(brand);

        JLabel role = new JLabel(user.role() == Role.ADMIN ? "RESTAURANT ADMIN" : "YOUR RESTAURANT");
        role.setFont(Theme.font(10, Font.BOLD));
        role.setForeground(new Color(178, 204, 184));
        role.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 0));
        sidebar.add(role);
        addNavigation(sidebar, "Overview", "Overview");
        addNavigation(sidebar, "Manage menu", "Manage menu");
        addNavigation(sidebar, "All orders", "All orders");
        addNavigation(sidebar, "Customers", "Customers");
        if (user.role() == Role.CUSTOMER) {
            sidebar.remove(sidebar.getComponentCount() - 1);
            sidebar.remove(sidebar.getComponentCount() - 1);
            sidebar.remove(sidebar.getComponentCount() - 1);
            sidebar.remove(sidebar.getComponentCount() - 1);
            addNavigation(sidebar, "Explore menu", "Menu");
            addNavigation(sidebar, "Your cart", "Cart");
            addNavigation(sidebar, "Order history", "Orders");
            cartCount.setFont(Theme.font(11, Font.PLAIN));
            cartCount.setForeground(new Color(202, 220, 207));
            cartCount.setBorder(BorderFactory.createEmptyBorder(1, 13, 8, 0));
            sidebar.add(cartCount);
        }
        sidebar.add(Box.createVerticalGlue());
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(user.fullName());
        name.setFont(Theme.font(13, Font.BOLD));
        name.setForeground(Color.WHITE);
        JLabel email = new JLabel(user.email());
        email.setFont(Theme.font(10, Font.PLAIN));
        email.setForeground(new Color(193, 212, 197));
        email.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        JButton logout = new JButton("Sign out");
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setForeground(Color.WHITE);
        logout.setBackground(Theme.GREEN_DARK);
        logout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(107, 145, 118)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        logout.addActionListener(event -> signOut());
        profile.add(name);
        profile.add(email);
        profile.add(logout);
        sidebar.add(profile);
        return sidebar;
    }

    private void addNavigation(JPanel sidebar, String label, String page) {
        JButton button = new JButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(Theme.font(13, Font.BOLD));
        button.setForeground(Color.WHITE);
        button.setBackground(Theme.GREEN_DARK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.addActionListener(event -> openPage(page));
        sidebar.add(button);
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(Theme.PAPER);
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(15, 26, 15, 26)));
        pageTitle.setFont(Theme.font(15, Font.BOLD));
        pageTitle.setForeground(Theme.INK);
        JLabel welcome = new JLabel(user.role() == Role.ADMIN ? "Restaurant workspace" : "Welcome, " + user.fullName());
        welcome.setFont(Theme.font(12, Font.PLAIN));
        welcome.setForeground(Theme.MUTED);
        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(welcome, BorderLayout.EAST);
        content.setBackground(Theme.PAPER);
        workspace.add(topBar, BorderLayout.NORTH);
        workspace.add(content, BorderLayout.CENTER);
        return workspace;
    }

    private void openPage(String page) {
        if (user.role() == Role.ADMIN && page.equals("Overview")) {
            content.removeAll();
            content.add(new AdminDashboardPanel(controller, user));
        } else if (user.role() == Role.CUSTOMER && page.equals("Menu")) {
            content.removeAll();
            content.add(new FoodMenuPanel(controller, user, this::refreshCartCount));
        } else if (user.role() == Role.CUSTOMER && page.equals("Cart")) {
            content.removeAll();
            content.add(new CartPanel(controller, user, () -> {
                refreshCartCount();
                openPage("Orders");
            }));
        } else if (page.equals("Orders") || page.equals("All orders")) {
            content.removeAll();
            content.add(new OrdersPanel(controller, user));
        } else if (user.role() == Role.ADMIN && page.equals("Manage menu")) {
            content.removeAll();
            content.add(new FoodManagementPanel(controller, user));
        } else if (user.role() == Role.ADMIN && page.equals("Customers")) {
            content.removeAll();
            content.add(new CustomerManagementPanel(controller, user));
        } else {
            return;
        }
        pageTitle.setText(page);
        content.revalidate();
        content.repaint();
    }

    private void refreshCartCount() {
        if (user.role() != Role.CUSTOMER) {
            return;
        }
        AsyncUi.run(content, () -> controller.cart(user).stream().mapToInt(item -> item.quantity()).sum(),
                count -> cartCount.setText(count + (count == 1 ? " item" : " items")));
    }

    private void signOut() {
        dispose();
        LoginFrame login = new LoginFrame(controller, signedIn -> {
            new MainFrame(controller, signedIn).setVisible(true);
        });
        login.setVisible(true);
    }
}
