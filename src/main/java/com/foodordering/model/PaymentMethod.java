package com.foodordering.model;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Cash on delivery"),
    DEMO_UPI("Demo UPI (not a real payment)");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
