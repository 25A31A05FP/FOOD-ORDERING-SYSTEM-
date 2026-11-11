package com.foodordering.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummary(
        long id,
        long userId,
        String customerName,
        OrderStatus status,
        BigDecimal total,
        PaymentMethod paymentMethod,
        LocalDateTime createdAt) {
}
