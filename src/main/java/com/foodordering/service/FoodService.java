package com.foodordering.service;

import com.foodordering.dao.FoodDao;
import com.foodordering.model.Category;
import com.foodordering.model.FoodItem;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import com.foodordering.util.Validation;
import java.sql.SQLException;
import java.util.List;

public class FoodService {
    private final FoodDao foodDao;

    public FoodService(FoodDao foodDao) {
        this.foodDao = foodDao;
    }

    public List<Category> categories() throws SQLException {
        return foodDao.findCategories();
    }

    public List<FoodItem> menu(String search, String category) throws SQLException {
        String cleanSearch = search == null ? "" : search.strip();
        if (cleanSearch.length() > 100) {
            throw new IllegalArgumentException("Search must be 100 characters or fewer.");
        }
        return foodDao.findAvailable(cleanSearch, category == null ? "" : category.strip());
    }

    public List<FoodItem> allForAdmin(User actor) throws SQLException {
        requireAdmin(actor);
        return foodDao.findAllForAdmin();
    }

    public void save(User actor, FoodItem item) throws SQLException {
        requireAdmin(actor);
        String name = Validation.requiredText(item.name(), "Food name", 100);
        String description = Validation.requiredText(item.description(), "Description", 500);
        Validation.positivePrice(item.price());
        if (item.categoryId() < 1) {
            throw new IllegalArgumentException("Select a food category.");
        }
        FoodItem clean = new FoodItem(item.id(), item.categoryId(), item.category(),
                name, description, item.price(), item.imagePath(), item.active());
        foodDao.save(clean);
    }

    public void setActive(User actor, long foodId, boolean active) throws SQLException {
        requireAdmin(actor);
        if (foodId < 1) {
            throw new IllegalArgumentException("Select a valid food item.");
        }
        foodDao.setActive(foodId, active);
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.role() != Role.ADMIN) {
            throw new SecurityException("Administrator access is required.");
        }
    }
}
