package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.Category;
import com.foodordering.model.FoodItem;
import com.foodordering.model.User;
import com.foodordering.util.Validation;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class FoodManagementPanel extends JPanel {
    private final AppController controller;
    private final User admin;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "Price", "Available"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private List<FoodItem> items = List.of();

    public FoodManagementPanel(AppController controller, User admin) {
        this.controller = controller;
        this.admin = admin;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        add(createHeader(), BorderLayout.NORTH);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
        reload();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new javax.swing.BoxLayout(title, javax.swing.BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Menu management");
        heading.setFont(Theme.font(26, Font.BOLD));
        heading.setForeground(Theme.INK);
        JLabel caption = new JLabel("Add, update or temporarily hide a dish from the customer menu.");
        caption.setFont(Theme.font(13, Font.PLAIN));
        caption.setForeground(Theme.MUTED);
        title.add(heading);
        title.add(caption);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        javax.swing.JButton add = Theme.button("Add dish", true);
        javax.swing.JButton edit = Theme.button("Edit", false);
        javax.swing.JButton toggle = Theme.button("Change availability", false);
        add.addActionListener(event -> editFood(null));
        edit.addActionListener(event -> editSelected());
        toggle.addActionListener(event -> toggleSelected());
        actions.add(add);
        actions.add(edit);
        actions.add(toggle);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    public void reload() {
        AsyncUi.run(this, () -> controller.adminMenu(admin), this::showItems);
    }

    private void showItems(List<FoodItem> foodItems) {
        items = List.copyOf(foodItems);
        tableModel.setRowCount(0);
        for (FoodItem item : items) {
            tableModel.addRow(new Object[]{item.id(), item.name(), item.category(),
                "$" + item.price().toPlainString(), item.active() ? "Available" : "Hidden"});
        }
    }

    private void editSelected() {
        FoodItem item = selectedItem();
        if (item != null) {
            editFood(item);
        }
    }

    private void toggleSelected() {
        FoodItem item = selectedItem();
        if (item == null) {
            return;
        }
        AsyncUi.run(this, () -> {
            controller.setFoodActive(admin, item.id(), !item.active());
            return Boolean.TRUE;
        }, ignored -> reload());
    }

    private FoodItem selectedItem() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a menu item first.", "No item selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int index = table.convertRowIndexToModel(selected);
        long id = ((Number) tableModel.getValueAt(index, 0)).longValue();
        return items.stream().filter(item -> item.id() == id).findFirst().orElse(null);
    }

    private void editFood(FoodItem existing) {
        AsyncUi.run(this, controller::categories, categories -> showEditor(existing, categories));
    }

    private void showEditor(FoodItem existing, List<Category> categories) {
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Create categories in the database before adding food.",
                    "No categories", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField name = new JTextField(existing == null ? "" : existing.name(), 24);
        JTextField description = new JTextField(existing == null ? "" : existing.description(), 24);
        JTextField price = new JTextField(existing == null ? "" : existing.price().toPlainString(), 12);
        JTextField imagePath = new JTextField(existing == null || existing.imagePath() == null
                ? "" : existing.imagePath(), 24);
        JComboBox<Category> category = new JComboBox<>(new DefaultComboBoxModel<>(categories.toArray(Category[]::new)));
        if (existing != null) {
            categories.stream().filter(item -> item.id() == existing.categoryId())
                    .findFirst().ifPresent(category::setSelectedItem);
        }
        JPanel form = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.insets = new java.awt.Insets(5, 6, 5, 6);
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        Object[][] fields = {
            {"Name", name}, {"Description", description}, {"Price", price},
            {"Category", category}, {"Image path (optional)", imagePath}
        };
        for (int i = 0; i < fields.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            form.add(new JLabel((String) fields[i][0]), c);
            c.gridx = 1;
            form.add((java.awt.Component) fields[i][1], c);
        }
        int result = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Add menu item" : "Edit menu item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Category selected = (Category) category.getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("Select a category.");
            }
            FoodItem item = new FoodItem(existing == null ? 0 : existing.id(), selected.id(), selected.name(),
                    Validation.requiredText(name.getText(), "Name", 100),
                    Validation.requiredText(description.getText(), "Description", 500),
                    Validation.positivePrice(new BigDecimal(price.getText().strip())),
                    imagePath.getText().strip().isEmpty() ? null : imagePath.getText().strip(),
                    existing == null || existing.active());
            AsyncUi.run(this, () -> {
                controller.saveFood(admin, item);
                return Boolean.TRUE;
            }, ignored -> reload());
        } catch (NumberFormatException exception) {
            Theme.showError(this, new IllegalArgumentException("Enter a valid price, such as 12.50.", exception));
        } catch (IllegalArgumentException exception) {
            Theme.showError(this, exception);
        }
    }
}
