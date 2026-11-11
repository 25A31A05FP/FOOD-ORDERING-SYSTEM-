package com.foodordering.service;

import com.foodordering.dao.UserDao;
import com.foodordering.model.User;
import com.foodordering.util.Validation;
import java.sql.SQLException;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User registerCustomer(String fullName, String email, char[] password) throws SQLException {
        String name = Validation.requiredText(fullName, "Name", 100);
        String cleanEmail = Validation.email(email);
        String cleanPassword = Validation.password(password);
        try {
            return userDao.createCustomer(name, cleanEmail, BCrypt.hashpw(cleanPassword, BCrypt.gensalt(12)));
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("An account with that email address already exists.", exception);
            }
            throw exception;
        }
    }

    public Optional<User> login(String email, char[] password) throws SQLException {
        String cleanEmail = Validation.email(email);
        String cleanPassword = Validation.password(password);
        Optional<UserDao.LoginRecord> record = userDao.findLoginRecord(cleanEmail);
        if (record.isEmpty() || !BCrypt.checkpw(cleanPassword, record.get().passwordHash())) {
            return Optional.empty();
        }
        return Optional.of(record.get().user());
    }
}
