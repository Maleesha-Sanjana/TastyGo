package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tastygo.model.User;
import com.tastygo.util.DateUtil;
import com.tastygo.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final DatabaseHelper dbHelper;

    public UserDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", user.getName());
        cv.put("email", user.getEmail().trim().toLowerCase());
        cv.put("phone", user.getPhone());
        cv.put("password_hash", user.getPasswordHash());
        cv.put("salt", user.getSalt());
        cv.put("role", user.getRole());
        cv.put("is_blocked", user.isBlocked() ? 1 : 0);
        cv.put("created_at", user.getCreatedAt().isEmpty() ? DateUtil.now() : user.getCreatedAt());

        return db.insert(DatabaseHelper.TABLE_USERS, null, cv);
    }

    public User findByEmail(String email) {
        if (email == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                "email = ?", new String[]{email.trim().toLowerCase()},
                null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public User findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                "id = ?", new String[]{String.valueOf(id)},
                null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public User verifyForPasswordReset(String email, String phone) {
        if (email == null || phone == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                "email = ? AND phone = ?",
                new String[]{email.trim().toLowerCase(), phone.trim()},
                null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public boolean updatePassword(long userId, String newHash, String newSalt) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password_hash", newHash);
        cv.put("salt", newSalt);

        int rows = db.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean updateProfile(long userId, String name, String phone) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);

        int rows = db.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean setBlocked(long userId, boolean blocked) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_blocked", blocked ? 1 : 0);

        int rows = db.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                null, null, null, null, "id DESC");

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToUser(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<User> searchUsers(String query) {
        List<User> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String wild = "%" + query.trim() + "%";
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                "name LIKE ? OR email LIKE ? OR phone LIKE ?",
                new String[]{wild, wild, wild}, null, null, "id DESC");

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToUser(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public int countTotalUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE role = 'USER'", null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
        user.setSalt(cursor.getString(cursor.getColumnIndexOrThrow("salt")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        user.setBlocked(cursor.getInt(cursor.getColumnIndexOrThrow("is_blocked")) == 1);
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return user;
    }
}
