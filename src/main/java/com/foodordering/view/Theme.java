package com.foodordering.view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public final class Theme {
    public static final Color INK = new Color(32, 38, 34);
    public static final Color MUTED = new Color(112, 121, 115);
    public static final Color PAPER = new Color(248, 248, 245);
    public static final Color WHITE = Color.WHITE;
    public static final Color GREEN = new Color(42, 105, 75);
    public static final Color GREEN_DARK = new Color(29, 79, 56);
    public static final Color GREEN_PALE = new Color(231, 242, 234);
    public static final Color LINE = new Color(229, 232, 227);
    public static final Color ORANGE = new Color(222, 129, 66);

    private Theme() {
    }

    public static void install() {
        UIManager.put("Panel.background", PAPER);
        UIManager.put("OptionPane.background", WHITE);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("PasswordField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Table.rowHeight", 38);
    }

    public static Font font(int size, int style) {
        return new Font("Segoe UI", style, size);
    }

    public static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(font(13, Font.BOLD));
        button.setForeground(primary ? WHITE : GREEN_DARK);
        button.setBackground(primary ? GREEN : WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? GREEN : LINE),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    public static void showError(JComponent parent, Exception exception) {
        exception.printStackTrace(System.err);
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception instanceof java.sql.SQLException
                    ? "The database request failed. Check that MySQL is running and your connection settings are correct."
                    : "The request could not be completed.";
        }
        JOptionPane.showMessageDialog(parent, message, "Something went wrong", JOptionPane.ERROR_MESSAGE);
    }
}
