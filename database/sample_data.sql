USE food_ordering_system;

INSERT INTO categories (name) VALUES
    ('Breakfast'),
    ('Lunch'),
    ('Dinner'),
    ('Snacks'),
    ('Beverages'),
    ('Desserts')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO food_items (category_id, name, description, price, image_path, active)
SELECT c.id, sample.name, sample.description, sample.price, NULL, TRUE
FROM (
    SELECT 'Breakfast' AS category_name, 'Avocado Toast' AS name,
           'Sourdough toast with smashed avocado, lemon and chilli flakes.' AS description, 8.50 AS price
    UNION ALL SELECT 'Breakfast', 'Fluffy Pancakes',
           'Golden buttermilk pancakes with maple syrup and fresh berries.', 9.25
    UNION ALL SELECT 'Lunch', 'Garden Buddha Bowl',
           'Roasted seasonal vegetables, quinoa and a bright tahini dressing.', 12.50
    UNION ALL SELECT 'Lunch', 'Crispy Chicken Sandwich',
           'Crispy chicken, crunchy slaw and house pickles on a toasted bun.', 13.75
    UNION ALL SELECT 'Dinner', 'Creamy Mushroom Pasta',
           'Silky parmesan sauce, roasted mushrooms and fresh parsley.', 16.50
    UNION ALL SELECT 'Dinner', 'Grilled Salmon Plate',
           'Grilled salmon with herbed potatoes and seasonal greens.', 19.95
    UNION ALL SELECT 'Snacks', 'Truffle Parmesan Fries',
           'Hand-cut golden fries finished with truffle oil and parmesan.', 7.50
    UNION ALL SELECT 'Snacks', 'Crispy Veggie Bites',
           'Crunchy vegetable bites served with a tangy herb dip.', 6.25
    UNION ALL SELECT 'Beverages', 'Fresh Berry Lemonade',
           'House-made lemonade with muddled berries and mint.', 4.50
    UNION ALL SELECT 'Beverages', 'Iced Matcha Latte',
           'Stone-ground matcha with chilled milk over ice.', 5.25
    UNION ALL SELECT 'Desserts', 'Chocolate Lava Cake',
           'Warm dark chocolate cake with a gooey molten centre.', 8.75
    UNION ALL SELECT 'Desserts', 'Strawberry Cheesecake',
           'Creamy vanilla cheesecake with fresh strawberry compote.', 8.25
) AS sample
JOIN categories c ON c.name = sample.category_name
WHERE NOT EXISTS (
    SELECT 1 FROM food_items existing
    WHERE existing.category_id = c.id AND existing.name = sample.name
);
