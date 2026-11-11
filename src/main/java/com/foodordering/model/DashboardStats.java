package com.foodordering.model;

import java.math.BigDecimal;

public record DashboardStats(long totalOrders, BigDecimal revenue, long totalCustomers) {
}
