package com.foodordering.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]{1,64}@[A-Za-z0-9.-]{1,190}\\.[A-Za-z]{2,}$");

    private Validation() {
    }

    public static String requiredText(String value, String label, int maxLength) {
        String cleaned = value == null ? "" : value.strip();
        if (cleaned.isEmpty() || cleaned.length() > maxLength) {
            throw new IllegalArgumentException(label + " is required and must be at most " + maxLength + " characters.");
        }
        return cleaned;
    }

    public static String email(String value) {
        String cleaned = requiredText(value, "Email", 254).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        return cleaned;
    }

    public static String password(char[] password) {
        if (password == null || password.length < 8 || password.length > 72
                || new String(password).getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain 8 to 72 characters.");
        }
        return new String(password);
    }

    public static BigDecimal positivePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0 || price.scale() > 2 || price.precision() > 10) {
            throw new IllegalArgumentException("Price must be positive and have no more than two decimal places.");
        }
        return price;
    }
}
