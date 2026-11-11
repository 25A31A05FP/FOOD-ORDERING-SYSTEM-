package com.foodordering.model;

import java.math.BigDecimal;

public record OrderLine(String itemName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
