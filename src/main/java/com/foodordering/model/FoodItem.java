package com.foodordering.model;

import java.math.BigDecimal;

public record FoodItem(
        long id,
        long categoryId,
        String category,
        String name,
        String description,
        BigDecimal price,
        String imagePath,
        boolean active) {
    public FoodItem {
        if (categoryId < 1 || category == null || category.isBlank()
                || name == null || name.isBlank()
                || description == null || price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("A food item needs a category, name, description and positive price.");
        }
    }
}
