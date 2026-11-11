package com.foodordering.model;

public record User(long id, String fullName, String email, Role role) {
    public User {
        if (id < 1 || fullName == null || email == null || role == null) {
            throw new IllegalArgumentException("A user needs an id, name, email and role.");
        }
    }
}
