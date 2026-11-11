CREATE DATABASE IF NOT EXISTS food_ordering_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE food_ordering_system;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS food_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image_path VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_food_price CHECK (price > 0),
    CONSTRAINT fk_food_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_food_category_active (category_id, active),
    INDEX idx_food_name (name)
);

CREATE TABLE IF NOT EXISTS cart_items (
    user_id BIGINT NOT NULL,
    food_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, food_item_id),
    CONSTRAINT chk_cart_quantity CHECK (quantity BETWEEN 1 AND 99),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_food FOREIGN KEY (food_item_id)
        REFERENCES food_items(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status ENUM(
        'PENDING', 'CONFIRMED', 'PREPARING',
        'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'
    ) NOT NULL DEFAULT 'PENDING',
    total DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_total CHECK (total >= 0),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_orders_user_date (user_id, created_at),
    INDEX idx_orders_status (status)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    food_item_id BIGINT NULL,
    item_name VARCHAR(100) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_order_item_price CHECK (unit_price > 0),
    CONSTRAINT chk_order_item_quantity CHECK (quantity BETWEEN 1 AND 99),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_food FOREIGN KEY (food_item_id)
        REFERENCES food_items(id) ON DELETE SET NULL,
    INDEX idx_order_items_order (order_id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL UNIQUE,
    method ENUM('CASH_ON_DELIVERY', 'DEMO_UPI') NOT NULL,
    status ENUM('PENDING', 'DEMO_ONLY', 'CANCELLED') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_amount CHECK (amount >= 0),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE
);
