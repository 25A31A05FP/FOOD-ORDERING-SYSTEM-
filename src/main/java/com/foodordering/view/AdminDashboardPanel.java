package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.DashboardStats;
import com.foodordering.model.User;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AdminDashboardPanel extends JPanel {
    private final AppController controller;
    private final User admin;
    private final JPanel metrics = new JPanel(new GridLayout(1, 3, 16, 16));

    public AdminDashboardPanel(AppController controller, User admin) {
        this.controller = controller;
        this.admin = admin;
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(28, 30, 30, 30));
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Good day, " + admin.fullName());
        title.setFont(Theme.font(27, Font.BOLD));
        title.setForeground(Theme.INK);
        JLabel subtitle = new JLabel("Here is what is happening in your restaurant today.");
        subtitle.setFont(Theme.font(13, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);
        metrics.setOpaque(false);
        metrics.setAlignmentY(TOP_ALIGNMENT);
        add(metrics, BorderLayout.CENTER);
        reload();
    }

    public void reload() {
        AsyncUi.run(this, () -> controller.dashboardStats(admin), this::showStats);
    }

    private void showStats(DashboardStats stats) {
        metrics.removeAll();
        metrics.add(metric("Total orders", Long.toString(stats.totalOrders()), "All time"));
        metrics.add(metric("Revenue", "$" + stats.revenue().toPlainString(), "Excluding cancelled orders"));
        metrics.add(metric("Customers", Long.toString(stats.totalCustomers()), "Registered accounts"));
        metrics.revalidate();
        metrics.repaint();
    }

    private JPanel metric(String label, String value, String detail) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        JPanel text = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        text.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setFont(Theme.font(13, Font.BOLD));
        title.setForeground(Theme.MUTED);
        text.add(title);
        JLabel amount = new JLabel(value);
        amount.setFont(Theme.font(30, Font.BOLD));
        amount.setForeground(Theme.GREEN);
        JLabel hint = new JLabel(detail);
        hint.setFont(Theme.font(12, Font.PLAIN));
        hint.setForeground(Theme.MUTED);
        card.add(text, BorderLayout.NORTH);
        card.add(amount, BorderLayout.CENTER);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }
}
