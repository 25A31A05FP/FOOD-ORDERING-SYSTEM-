package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.CartItem;
import com.foodordering.model.OrderReceipt;
import com.foodordering.model.PaymentMethod;
import com.foodordering.model.User;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CartPanel extends JPanel {
    private final AppController controller;
    private final User customer;
    private final Runnable orderPlaced;
    private final JPanel itemsPanel = new JPanel();
    private final JLabel subtotal = new JLabel("$0.00");
    private final JLabel total = new JLabel("$0.00");
    private final JComboBox<PaymentMethod> paymentMethod = new JComboBox<>(PaymentMethod.values());
    private final javax.swing.JButton checkout = Theme.button("Place order", true);

    public CartPanel(AppController controller, User customer, Runnable orderPlaced) {
        this.controller = controller;
        this.customer = customer;
        this.orderPlaced = orderPlaced;
        setLayout(new BorderLayout(18, 18));
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        add(createHeading(), BorderLayout.NORTH);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Theme.PAPER);
        add(new JScrollPane(itemsPanel), BorderLayout.CENTER);
        add(createSummary(), BorderLayout.EAST);
        checkout.addActionListener(event -> placeOrder());
        reload();
    }

    private JLabel createHeading() {
        JLabel heading = new JLabel("Your cart");
        heading.setFont(Theme.font(26, Font.BOLD));
        heading.setForeground(Theme.INK);
        return heading;
    }

    private JPanel createSummary() {
        JPanel summary = new JPanel(new BorderLayout(0, 14));
        summary.setPreferredSize(new java.awt.Dimension(290, 230));
        summary.setBackground(Theme.WHITE);
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JPanel amounts = new JPanel(new GridLayout(0, 1, 0, 10));
        amounts.setOpaque(false);
        JLabel title = new JLabel("Order summary");
        title.setFont(Theme.font(17, Font.BOLD));
        title.setForeground(Theme.INK);
        JPanel subtotalRow = new JPanel(new BorderLayout());
        subtotalRow.setOpaque(false);
        JLabel label = new JLabel("Subtotal");
        label.setForeground(Theme.MUTED);
        subtotal.setFont(Theme.font(15, Font.BOLD));
        subtotal.setForeground(Theme.INK);
        subtotalRow.add(label, BorderLayout.WEST);
        subtotalRow.add(subtotal, BorderLayout.EAST);
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalLabel = new JLabel("Total");
        totalLabel.setFont(Theme.font(15, Font.BOLD));
        total.setFont(Theme.font(17, Font.BOLD));
        total.setForeground(Theme.GREEN);
        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(total, BorderLayout.EAST);
        amounts.add(title);
        amounts.add(subtotalRow);
        amounts.add(totalRow);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JLabel paymentLabel = new JLabel("Payment method");
        paymentLabel.setFont(Theme.font(12, Font.BOLD));
        paymentLabel.setAlignmentX(LEFT_ALIGNMENT);
        paymentMethod.setRenderer((list, value, index, selected, focused) -> {
            JLabel option = new JLabel(value == null ? "" : value.label());
            option.setOpaque(true);
            option.setBackground(selected ? Theme.GREEN_PALE : Theme.WHITE);
            option.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
            option.setFont(Theme.font(12, Font.PLAIN));
            return option;
        });
        paymentMethod.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
        checkout.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(paymentLabel);
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(paymentMethod);
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(checkout);
        JLabel disclaimer = new JLabel("<html>Demo UPI is a test selection only.<br>No real payment is processed.</html>");
        disclaimer.setFont(Theme.font(10, Font.PLAIN));
        disclaimer.setForeground(Theme.MUTED);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(disclaimer);

        summary.add(amounts, BorderLayout.NORTH);
        summary.add(bottom, BorderLayout.CENTER);
        return summary;
    }

    public void reload() {
        AsyncUi.run(this, () -> controller.cart(customer), this::showItems);
    }

    private void showItems(List<CartItem> items) {
        itemsPanel.removeAll();
        java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
        for (CartItem item : items) {
            sum = sum.add(item.lineTotal());
            itemsPanel.add(createItemRow(item));
            itemsPanel.add(Box.createVerticalStrut(10));
        }
        if (items.isEmpty()) {
            JLabel empty = new JLabel("Your cart is empty. Browse the menu to add something delicious.");
            empty.setFont(Theme.font(15, Font.PLAIN));
            empty.setForeground(Theme.MUTED);
            itemsPanel.add(empty);
        }
        String amount = "$" + sum.toPlainString();
        subtotal.setText(amount);
        total.setText(amount);
        checkout.setEnabled(!items.isEmpty());
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createItemRow(CartItem item) {
        JPanel row = new JPanel(new BorderLayout(14, 8));
        row.setBackground(Theme.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(13, 14, 13, 14)));
        JPanel description = new JPanel();
        description.setOpaque(false);
        description.setLayout(new BoxLayout(description, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(item.food().name());
        name.setFont(Theme.font(15, Font.BOLD));
        JLabel unit = new JLabel("$" + item.food().price().toPlainString() + " each");
        unit.setFont(Theme.font(12, Font.PLAIN));
        unit.setForeground(Theme.MUTED);
        JLabel lineTotal = new JLabel("$" + item.lineTotal().toPlainString());
        lineTotal.setFont(Theme.font(14, Font.BOLD));
        lineTotal.setForeground(Theme.GREEN);
        description.add(name);
        description.add(Box.createVerticalStrut(4));
        description.add(unit);
        description.add(Box.createVerticalStrut(4));
        description.add(lineTotal);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controls.setOpaque(false);
        javax.swing.JButton decrement = Theme.button("−", false);
        JLabel quantity = new JLabel(Integer.toString(item.quantity()));
        quantity.setFont(Theme.font(14, Font.BOLD));
        javax.swing.JButton increment = Theme.button("+", false);
        javax.swing.JButton remove = Theme.button("Remove", false);
        decrement.addActionListener(event -> changeQuantity(item, item.quantity() - 1));
        increment.addActionListener(event -> changeQuantity(item, item.quantity() + 1));
        remove.addActionListener(event -> AsyncUi.run(this, () -> {
            controller.removeFromCart(customer, item.food().id());
            return Boolean.TRUE;
        }, ignored -> reload()));
        increment.setEnabled(item.quantity() < 99);
        controls.add(decrement);
        controls.add(quantity);
        controls.add(increment);
        controls.add(remove);
        row.add(description, BorderLayout.CENTER);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private void changeQuantity(CartItem item, int quantity) {
        AsyncUi.run(this, () -> {
            controller.setCartQuantity(customer, item.food().id(), quantity);
            return Boolean.TRUE;
        }, ignored -> reload());
    }

    private void placeOrder() {
        PaymentMethod method = (PaymentMethod) paymentMethod.getSelectedItem();
        if (method == null) {
            return;
        }
        AsyncUi.run(this, () -> controller.checkout(customer, method), receipt -> {
            showReceipt(receipt);
            orderPlaced.run();
        });
    }

    private void showReceipt(OrderReceipt receipt) {
        StringBuilder text = new StringBuilder();
        text.append("Order #").append(receipt.order().id()).append('\n');
        text.append("Status: ").append(receipt.order().status().label()).append("\n\n");
        receipt.items().forEach(item -> text.append(item.itemName()).append(" × ")
                .append(item.quantity()).append("  $").append(item.lineTotal().toPlainString()).append('\n'));
        text.append("\nTotal: $").append(receipt.order().total().toPlainString());
        text.append("\nPayment: ").append(receipt.order().paymentMethod().label());
        JOptionPane.showMessageDialog(this, text.toString(), "Order receipt", JOptionPane.INFORMATION_MESSAGE);
    }
}
