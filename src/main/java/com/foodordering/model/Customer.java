package com.foodordering.model;

public record Customer(long id, String fullName, String email, boolean active) {
}
