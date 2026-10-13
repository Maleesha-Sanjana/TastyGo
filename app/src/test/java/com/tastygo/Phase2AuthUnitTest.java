package com.tastygo;

import org.junit.Test;

import static org.junit.Assert.*;

import com.tastygo.model.User;
import com.tastygo.util.PasswordUtil;

public class Phase2AuthUnitTest {

    @Test
    public void testPasswordSaltAndHashing() {
        String plainPassword = "MySecurePassword123";
        String salt = PasswordUtil.generateSalt();

        assertNotNull(salt);
        assertEquals(32, salt.length()); // 16 bytes = 32 hex chars

        String hash = PasswordUtil.hashPassword(plainPassword, salt);
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 = 64 hex chars

        // Verify correct password
        assertTrue(PasswordUtil.verifyPassword(plainPassword, salt, hash));

        // Verify incorrect password
        assertFalse(PasswordUtil.verifyPassword("WrongPassword123", salt, hash));
        assertFalse(PasswordUtil.verifyPassword("mysecurepassword123", salt, hash)); // Case sensitive
    }

    @Test
    public void testUserRoleAndBlockedModel() {
        User admin = new User();
        admin.setRole("ADMIN");
        admin.setBlocked(false);
        assertTrue(admin.isAdmin());
        assertFalse(admin.isBlocked());

        User regularUser = new User();
        regularUser.setRole("USER");
        regularUser.setBlocked(true);
        assertFalse(regularUser.isAdmin());
        assertTrue(regularUser.isBlocked());
    }

    @Test
    public void testPasswordResetFlowSimulation() {
        // Step 1: User registered with original password
        String originalPassword = "InitialPassword123";
        String salt1 = PasswordUtil.generateSalt();
        String hash1 = PasswordUtil.hashPassword(originalPassword, salt1);

        User user = new User("Jane Doe", "jane@example.com", "+1 555 123 4567", hash1, salt1, "USER", false, "2026-10-13 10:00:00");
        assertTrue(PasswordUtil.verifyPassword(originalPassword, user.getSalt(), user.getPasswordHash()));

        // Step 2: Verification by email + phone
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("+1 555 123 4567", user.getPhone());

        // Step 3: Password reset with new salt & hash
        String newPassword = "NewSecretPassword2026";
        String salt2 = PasswordUtil.generateSalt();
        String hash2 = PasswordUtil.hashPassword(newPassword, salt2);

        user.setSalt(salt2);
        user.setPasswordHash(hash2);

        // Verification of update
        assertTrue(PasswordUtil.verifyPassword(newPassword, user.getSalt(), user.getPasswordHash()));
        assertFalse(PasswordUtil.verifyPassword(originalPassword, user.getSalt(), user.getPasswordHash()));
    }
}
