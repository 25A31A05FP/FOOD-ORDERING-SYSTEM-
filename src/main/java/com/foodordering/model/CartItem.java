package com.foodordering.model;

import java.math.BigDecimal;

public record CartItem(FoodItem food, int quantity) {
    public CartItem {
        if (food == null || quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Cart quantity must be between 1 and 99.");
        }
    }

    public BigDecimal lineTotal() {
        return food.price().multiply(BigDecimal.valueOf(quantity));
    }
}
