package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.Customer;
import com.foodordering.model.User;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class CustomerManagementPanel extends JPanel {
    private final AppController controller;
    private final User admin;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Customer", "Email", "Account"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<Customer> customers = List.of();

    public CustomerManagementPanel(AppController controller, User admin) {
        this.controller = controller;
        this.admin = admin;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        add(createHeader(), BorderLayout.NORTH);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        reload();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new javax.swing.BoxLayout(title, javax.swing.BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Customers");
        heading.setFont(Theme.font(26, Font.BOLD));
        heading.setForeground(Theme.INK);
        JLabel caption = new JLabel("Review registered accounts and enable or pause access.");
        caption.setFont(Theme.font(13, Font.PLAIN));
        caption.setForeground(Theme.MUTED);
        title.add(heading);
        title.add(caption);
        javax.swing.JButton toggle = Theme.button("Enable / disable account", false);
        toggle.addActionListener(event -> toggleSelected());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(toggle);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    public void reload() {
        AsyncUi.run(this, () -> controller.customers(admin), this::showCustomers);
    }

    private void showCustomers(List<Customer> items) {
        customers = List.copyOf(items);
        tableModel.setRowCount(0);
        for (Customer customer : customers) {
            tableModel.addRow(new Object[]{customer.id(), customer.fullName(), customer.email(),
                customer.active() ? "Active" : "Disabled"});
        }
    }

    private void toggleSelected() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Select a customer first.", "No customer selected",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        long id = ((Number) tableModel.getValueAt(table.convertRowIndexToModel(selected), 0)).longValue();
        Customer customer = customers.stream().filter(item -> item.id() == id).findFirst().orElseThrow();
        AsyncUi.run(this, () -> {
            controller.setCustomerActive(admin, customer.id(), !customer.active());
            return Boolean.TRUE;
        }, ignored -> reload());
    }
}
