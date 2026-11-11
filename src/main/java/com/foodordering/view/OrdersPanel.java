package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.OrderStatus;
import com.foodordering.model.OrderSummary;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class OrdersPanel extends JPanel {
    private final AppController controller;
    private final User user;
    private final boolean admin;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<OrderStatus> status = new JComboBox<>(OrderStatus.values());

    public OrdersPanel(AppController controller, User user) {
        this.controller = controller;
        this.user = user;
        this.admin = user.role() == Role.ADMIN;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        add(createHeader(), BorderLayout.NORTH);
        String[] columns = admin
                ? new String[]{"Order", "Customer", "Placed", "Payment", "Total", "Status"}
                : new String[]{"Order", "Placed", "Payment", "Total", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        add(scroll, BorderLayout.CENTER);
        reload();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new javax.swing.BoxLayout(titles, javax.swing.BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(admin ? "All orders" : "Your orders");
        heading.setFont(Theme.font(26, Font.BOLD));
        heading.setForeground(Theme.INK);
        JLabel subheading = new JLabel(admin ? "Review orders and update their delivery status."
                : "Follow each order from the kitchen to your door.");
        subheading.setFont(Theme.font(13, Font.PLAIN));
        subheading.setForeground(Theme.MUTED);
        titles.add(heading);
        titles.add(subheading);
        header.add(titles, BorderLayout.WEST);
        if (admin) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);
            status.setRenderer((list, value, index, selected, focused) -> {
                JLabel option = new JLabel(value == null ? "" : value.label());
                option.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                option.setOpaque(true);
                option.setBackground(selected ? Theme.GREEN_PALE : Theme.WHITE);
                return option;
            });
            javax.swing.JButton update = Theme.button("Update status", true);
            update.addActionListener(event -> updateSelected());
            actions.add(status);
            actions.add(update);
            header.add(actions, BorderLayout.EAST);
        }
        return header;
    }

    public void reload() {
        AsyncUi.run(this, () -> admin ? controller.allOrders(user) : controller.orderHistory(user), this::showOrders);
    }

    private void showOrders(List<OrderSummary> orders) {
        tableModel.setRowCount(0);
        for (OrderSummary order : orders) {
            Object[] row = admin
                    ? new Object[]{order.id(), order.customerName(), order.createdAt().toString().replace('T', ' '),
                        order.paymentMethod().label(), "$" + order.total().toPlainString(), order.status().label()}
                    : new Object[]{order.id(), order.createdAt().toString().replace('T', ' '),
                        order.paymentMethod().label(), "$" + order.total().toPlainString(), order.status().label()};
            tableModel.addRow(row);
        }
    }

    private void updateSelected() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select an order first.", "No order selected",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        long orderId = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(selected), 0)).longValue();
        OrderStatus next = (OrderStatus) status.getSelectedItem();
        AsyncUi.run(this, () -> {
            controller.updateOrderStatus(user, orderId, next);
            return Boolean.TRUE;
        }, ignored -> reload());
    }
}
