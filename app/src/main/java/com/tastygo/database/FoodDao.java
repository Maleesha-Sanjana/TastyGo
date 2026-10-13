package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tastygo.model.Category;
import com.tastygo.model.FoodItem;
import com.tastygo.model.FoodOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for food items, categories, options, and favorites.
 */
public class FoodDao {

    private final DatabaseHelper dbHelper;

    public FoodDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    // ================= CATEGORIES =================

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null,
                null, null, null, null, "id ASC");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new Category(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public Category getCategoryById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null,
                "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return new Category(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public long insertCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", category.getName());
        cv.put("image_path", category.getImagePath());
        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);
    }

    public boolean updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", category.getName());
        cv.put("image_path", category.getImagePath());
        return db.update(DatabaseHelper.TABLE_CATEGORIES, cv, "id = ?",
                new String[]{String.valueOf(category.getId())}) > 0;
    }

    public boolean deleteCategory(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CATEGORIES, "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    // ================= FOOD ITEMS =================

    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT f.*, c.name AS category_name FROM " + DatabaseHelper.TABLE_FOOD_ITEMS + " f " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON f.category_id = c.id " +
                "ORDER BY f.id DESC";

        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToFood(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<FoodItem> getAvailableFoodItems() {
        List<FoodItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT f.*, c.name AS category_name FROM " + DatabaseHelper.TABLE_FOOD_ITEMS + " f " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON f.category_id = c.id " +
                "WHERE f.is_available = 1 " +
                "ORDER BY f.avg_rating DESC, f.id DESC";

        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToFood(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public FoodItem getFoodById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT f.*, c.name AS category_name FROM " + DatabaseHelper.TABLE_FOOD_ITEMS + " f " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON f.category_id = c.id " +
                "WHERE f.id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                FoodItem item = cursorToFood(cursor);
                item.setOptions(getOptionsForFood(id));
                return item;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public List<FoodItem> getFoodItemsByCategory(long categoryId) {
        List<FoodItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT f.*, c.name AS category_name FROM " + DatabaseHelper.TABLE_FOOD_ITEMS + " f " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON f.category_id = c.id " +
                "WHERE f.category_id = ? " +
                "ORDER BY f.id DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToFood(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<FoodItem> searchAndFilter(String query, Long categoryId, Boolean vegOnly,
                                         Double minPrice, Double maxPrice, String sortBy) {
        List<FoodItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT f.*, c.name AS category_name FROM ").append(DatabaseHelper.TABLE_FOOD_ITEMS).append(" f ");
        sb.append("LEFT JOIN ").append(DatabaseHelper.TABLE_CATEGORIES).append(" c ON f.category_id = c.id ");
        sb.append("WHERE 1=1 ");

        List<String> args = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sb.append("AND (f.name LIKE ? OR f.description LIKE ?) ");
            String wild = "%" + query.trim() + "%";
            args.add(wild);
            args.add(wild);
        }
        if (categoryId != null && categoryId > 0) {
            sb.append("AND f.category_id = ? ");
            args.add(String.valueOf(categoryId));
        }
        if (vegOnly != null && vegOnly) {
            sb.append("AND f.is_veg = 1 ");
        }
        if (minPrice != null) {
            sb.append("AND f.price >= ? ");
            args.add(String.valueOf(minPrice));
        }
        if (maxPrice != null) {
            sb.append("AND f.price <= ? ");
            args.add(String.valueOf(maxPrice));
        }

        if ("PRICE_ASC".equalsIgnoreCase(sortBy)) {
            sb.append("ORDER BY f.price ASC ");
        } else if ("PRICE_DESC".equalsIgnoreCase(sortBy)) {
            sb.append("ORDER BY f.price DESC ");
        } else if ("RATING_DESC".equalsIgnoreCase(sortBy)) {
            sb.append("ORDER BY f.avg_rating DESC ");
        } else {
            sb.append("ORDER BY f.id DESC ");
        }

        Cursor cursor = db.rawQuery(sb.toString(), args.toArray(new String[0]));
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToFood(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public long insertFood(FoodItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category_id", item.getCategoryId());
        cv.put("name", item.getName());
        cv.put("description", item.getDescription());
        cv.put("price", item.getPrice());
        cv.put("image_path", item.getImagePath());
        cv.put("is_available", item.isAvailable() ? 1 : 0);
        cv.put("is_veg", item.isVeg() ? 1 : 0);
        cv.put("avg_rating", item.getAvgRating());

        return db.insert(DatabaseHelper.TABLE_FOOD_ITEMS, null, cv);
    }

    public boolean updateFood(FoodItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("category_id", item.getCategoryId());
        cv.put("name", item.getName());
        cv.put("description", item.getDescription());
        cv.put("price", item.getPrice());
        cv.put("image_path", item.getImagePath());
        cv.put("is_available", item.isAvailable() ? 1 : 0);
        cv.put("is_veg", item.isVeg() ? 1 : 0);

        return db.update(DatabaseHelper.TABLE_FOOD_ITEMS, cv, "id = ?",
                new String[]{String.valueOf(item.getId())}) > 0;
    }

    public boolean setAvailability(long foodId, boolean available) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_available", available ? 1 : 0);
        return db.update(DatabaseHelper.TABLE_FOOD_ITEMS, cv, "id = ?",
                new String[]{String.valueOf(foodId)}) > 0;
    }

    public boolean deleteFood(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_FOOD_ITEMS, "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    // ================= OPTIONS =================

    public List<FoodOption> getOptionsForFood(long foodId) {
        List<FoodOption> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FOOD_OPTIONS, null,
                "food_id = ?", new String[]{String.valueOf(foodId)},
                null, null, "type DESC, extra_price ASC");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new FoodOption(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("food_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("type")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("extra_price"))
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public long insertOption(FoodOption option) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("food_id", option.getFoodId());
        cv.put("type", option.getType());
        cv.put("name", option.getName());
        cv.put("extra_price", option.getExtraPrice());
        return db.insert(DatabaseHelper.TABLE_FOOD_OPTIONS, null, cv);
    }

    public void deleteOptionsForFood(long foodId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_FOOD_OPTIONS, "food_id = ?", new String[]{String.valueOf(foodId)});
    }

    // ================= FAVORITES =================

    public boolean isFavorite(long userId, long foodId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FAVORITES, null,
                "user_id = ? AND food_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(foodId)},
                null, null, null);
        try {
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public boolean toggleFavorite(long userId, long foodId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (isFavorite(userId, foodId)) {
            db.delete(DatabaseHelper.TABLE_FAVORITES, "user_id = ? AND food_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(foodId)});
            return false;
        } else {
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("food_id", foodId);
            db.insert(DatabaseHelper.TABLE_FAVORITES, null, cv);
            return true;
        }
    }

    public List<FoodItem> getFavoriteFoods(long userId) {
        List<FoodItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT f.*, c.name AS category_name FROM " + DatabaseHelper.TABLE_FOOD_ITEMS + " f " +
                "INNER JOIN " + DatabaseHelper.TABLE_FAVORITES + " fav ON f.id = fav.food_id " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c ON f.category_id = c.id " +
                "WHERE fav.user_id = ? " +
                "ORDER BY f.name ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    FoodItem item = cursorToFood(cursor);
                    item.setFavorite(true);
                    list.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public void recalculateAverageRating(long foodId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(rating) FROM " + DatabaseHelper.TABLE_REVIEWS + " WHERE food_id = ?",
                new String[]{String.valueOf(foodId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                double avg = cursor.getDouble(0);
                ContentValues cv = new ContentValues();
                cv.put("avg_rating", Math.round(avg * 10.0) / 10.0);
                db.update(DatabaseHelper.TABLE_FOOD_ITEMS, cv, "id = ?", new String[]{String.valueOf(foodId)});
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private FoodItem cursorToFood(Cursor cursor) {
        FoodItem item = new FoodItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        item.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow("category_id")));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
        item.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
        item.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow("is_available")) == 1);
        item.setVeg(cursor.getInt(cursor.getColumnIndexOrThrow("is_veg")) == 1);
        item.setAvgRating(cursor.getDouble(cursor.getColumnIndexOrThrow("avg_rating")));

        int catIdx = cursor.getColumnIndex("category_name");
        if (catIdx != -1) {
            item.setCategoryName(cursor.getString(catIdx));
        }
        return item;
    }
}
