package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.Category;
import com.foodordering.model.FoodItem;
import com.foodordering.model.User;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;

public class FoodMenuPanel extends JPanel {
    private final AppController controller;
    private final User customer;
    private final Runnable cartChanged;
    private final JPanel cards = new JPanel(new GridLayout(0, 3, 16, 16));
    private final JTextField search = new JTextField(22);
    private final JComboBox<String> category = new JComboBox<>();
    private final JLabel count = new JLabel(" ");
    private final Timer searchTimer;

    public FoodMenuPanel(AppController controller, User customer, Runnable cartChanged) {
        this.controller = controller;
        this.customer = customer;
        this.cartChanged = cartChanged;
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        add(createHeader(), BorderLayout.NORTH);
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(new JScrollPane(cards), BorderLayout.CENTER);
        searchTimer = new Timer(300, event -> reload());
        searchTimer.setRepeats(false);
        search.addActionListener(event -> reload());
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                searchTimer.restart();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                searchTimer.restart();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                searchTimer.restart();
            }
        });
        category.addActionListener(event -> reload());
        loadCategories();
        reload();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new javax.swing.BoxLayout(title, javax.swing.BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Find your next favourite");
        heading.setFont(Theme.font(26, Font.BOLD));
        heading.setForeground(Theme.INK);
        JLabel subtitle = new JLabel("Freshly prepared, made for your table.");
        subtitle.setFont(Theme.font(13, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        title.add(heading);
        title.add(subtitle);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);
        search.setToolTipText("Search by dish or description");
        search.setPreferredSize(new java.awt.Dimension(220, 40));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        category.setPreferredSize(new java.awt.Dimension(150, 40));
        filters.add(search);
        filters.add(category);
        header.add(title, BorderLayout.WEST);
        header.add(filters, BorderLayout.EAST);
        header.add(count, BorderLayout.SOUTH);
        count.setFont(Theme.font(11, Font.PLAIN));
        count.setForeground(Theme.MUTED);
        return header;
    }

    public void reload() {
        String selected = (String) category.getSelectedItem();
        String categoryName = selected == null || selected.equals("All categories") ? "" : selected;
        AsyncUi.run(this, () -> controller.menu(search.getText(), categoryName), this::showItems);
    }

    private void loadCategories() {
        AsyncUi.run(this, controller::categories, categories -> {
            category.removeAllItems();
            category.addItem("All categories");
            for (Category item : categories) {
                category.addItem(item.name());
            }
        });
    }

    private void showItems(List<FoodItem> items) {
        cards.removeAll();
        count.setText(items.size() + (items.size() == 1 ? " dish" : " dishes"));
        if (items.isEmpty()) {
            JLabel empty = new JLabel("No dishes match your search. Try another name or category.");
            empty.setFont(Theme.font(15, Font.PLAIN));
            empty.setForeground(Theme.MUTED);
            cards.add(empty);
        } else {
            for (FoodItem item : items) {
                cards.add(new FoodCard(item, food -> AsyncUi.run(this,
                        () -> {
                            controller.addToCart(customer, food.id());
                            return Boolean.TRUE;
                        }, ignored -> {
                            cartChanged.run();
                            javax.swing.JOptionPane.showMessageDialog(this,
                                    food.name() + " added to your cart.", "Added to cart",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        })));
            }
        }
        cards.revalidate();
        cards.repaint();
    }
}
