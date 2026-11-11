package com.foodordering.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

class PasswordHashTest {
    @Test
    void bcryptHashVerifiesOnlyTheMatchingPassword() {
        String hash = BCrypt.hashpw("Correct horse 42!", BCrypt.gensalt(10));

        assertTrue(BCrypt.checkpw("Correct horse 42!", hash));
        assertFalse(BCrypt.checkpw("different password", hash));
        assertFalse(hash.contains("Correct horse 42!"));
    }
}
