package com.foodordering.view;

import com.foodordering.model.FoodItem;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FoodCard extends JPanel {
    public FoodCard(FoodItem food, Consumer<FoodItem> onAdd) {
        setBackground(Theme.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(10, 10, 12, 10)));
        setLayout(new BorderLayout(0, 9));
        FoodIllustration illustration = new FoodIllustration(food);
        illustration.setPreferredSize(new Dimension(220, 145));
        add(illustration, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(0, 1, 0, 5));
        details.setOpaque(false);
        JLabel category = new JLabel(food.category().toUpperCase());
        category.setFont(Theme.font(10, Font.BOLD));
        category.setForeground(Theme.GREEN);
        JLabel name = new JLabel(food.name());
        name.setFont(Theme.font(16, Font.BOLD));
        name.setForeground(Theme.INK);
        JLabel description = new JLabel("<html><div style='width:205px'>" + escape(food.description()) + "</div></html>");
        description.setFont(Theme.font(12, Font.PLAIN));
        description.setForeground(Theme.MUTED);
        JLabel price = new JLabel("$" + food.price().toPlainString());
        price.setFont(Theme.font(17, Font.BOLD));
        price.setForeground(Theme.GREEN_DARK);
        details.add(category);
        details.add(name);
        details.add(description);
        details.add(price);
        add(details, BorderLayout.CENTER);

        javax.swing.JButton addButton = Theme.button("Add to cart", true);
        addButton.addActionListener(event -> onAdd.accept(food));
        add(addButton, BorderLayout.SOUTH);
    }

    private String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
