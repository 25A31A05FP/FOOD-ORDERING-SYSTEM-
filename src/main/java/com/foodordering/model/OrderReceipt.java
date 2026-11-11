package com.foodordering.model;

import java.util.List;

public record OrderReceipt(OrderSummary order, List<OrderLine> items) {
    public OrderReceipt {
        items = List.copyOf(items);
    }
}
