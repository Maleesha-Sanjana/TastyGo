package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tastygo.model.Review;
import com.tastygo.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for reviews table.
 */
public class ReviewDao {

    private final DatabaseHelper dbHelper;
    private final FoodDao foodDao;

    public ReviewDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.foodDao = new FoodDao(context);
    }

    public List<Review> getReviewsForFood(long foodId) {
        List<Review> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT r.*, u.name AS user_name, f.name AS food_name " +
                "FROM " + DatabaseHelper.TABLE_REVIEWS + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON r.user_id = u.id " +
                "INNER JOIN " + DatabaseHelper.TABLE_FOOD_ITEMS + " f ON r.food_id = f.id " +
                "WHERE r.food_id = ? " +
                "ORDER BY r.id DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(foodId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToReview(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<Review> getAllReviews() {
        List<Review> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT r.*, u.name AS user_name, f.name AS food_name " +
                "FROM " + DatabaseHelper.TABLE_REVIEWS + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON r.user_id = u.id " +
                "INNER JOIN " + DatabaseHelper.TABLE_FOOD_ITEMS + " f ON r.food_id = f.id " +
                "ORDER BY r.id DESC";

        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToReview(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Checks if the user is eligible to review: must have ordered this item in a DELIVERED order.
     */
    public boolean canUserReview(long userId, long foodId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS + " o " +
                "INNER JOIN " + DatabaseHelper.TABLE_ORDER_ITEMS + " oi ON o.id = oi.order_id " +
                "WHERE o.user_id = ? AND oi.food_id = ? AND o.status = 'DELIVERED'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(foodId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    public long insertReview(Review review) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", review.getUserId());
        cv.put("food_id", review.getFoodId());
        cv.put("rating", review.getRating());
        cv.put("comment", review.getComment());
        cv.put("created_at", review.getCreatedAt().isEmpty() ? DateUtil.now() : review.getCreatedAt());

        long rowId = db.insert(DatabaseHelper.TABLE_REVIEWS, null, cv);
        if (rowId != -1) {
            foodDao.recalculateAverageRating(review.getFoodId());
        }
        return rowId;
    }

    public boolean deleteReview(long reviewId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long foodId = -1;
        Cursor cursor = db.query(DatabaseHelper.TABLE_REVIEWS, new String[]{"food_id"},
                "id = ?", new String[]{String.valueOf(reviewId)}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                foodId = cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        int rows = db.delete(DatabaseHelper.TABLE_REVIEWS, "id = ?", new String[]{String.valueOf(reviewId)});
        if (rows > 0 && foodId != -1) {
            foodDao.recalculateAverageRating(foodId);
        }
        return rows > 0;
    }

    private Review cursorToReview(Cursor cursor) {
        Review r = new Review();
        r.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        r.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        r.setFoodId(cursor.getLong(cursor.getColumnIndexOrThrow("food_id")));
        r.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow("rating")));
        r.setComment(cursor.getString(cursor.getColumnIndexOrThrow("comment")));
        r.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));

        int userIdx = cursor.getColumnIndex("user_name");
        if (userIdx != -1) r.setUserName(cursor.getString(userIdx));
        int foodIdx = cursor.getColumnIndex("food_name");
        if (foodIdx != -1) r.setFoodName(cursor.getString(foodIdx));
        return r;
    }
}
