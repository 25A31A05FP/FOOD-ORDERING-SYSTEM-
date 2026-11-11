package com.foodordering.model;

public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PREPARING("Preparing"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static OrderStatus fromDatabase(String value) {
        return valueOf(value);
    }
}
