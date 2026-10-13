package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for settings table.
 */
public class SettingsDao {

    public static final String KEY_RESTAURANT_NAME = "restaurant_name";
    public static final String KEY_OPENING_HOURS = "opening_hours";
    public static final String KEY_DELIVERY_FEE = "delivery_fee";
    public static final String KEY_TAX_RATE = "tax_rate";
    public static final String KEY_ACCEPTING_ORDERS = "accepting_orders";

    private final DatabaseHelper dbHelper;

    public SettingsDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public String get(String key, String defaultValue) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SETTINGS, new String[]{"value"},
                "key = ?", new String[]{key}, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String val = cursor.getString(0);
                return val != null ? val : defaultValue;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return defaultValue;
    }

    public void set(String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict(DatabaseHelper.TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Map<String, String> getAll() {
        Map<String, String> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SETTINGS, null, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    map.put(cursor.getString(0), cursor.getString(1));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return map;
    }

    public double getDeliveryFee() {
        try {
            return Double.parseDouble(get(KEY_DELIVERY_FEE, "3.99"));
        } catch (NumberFormatException e) {
            return 3.99;
        }
    }

    public double getTaxRate() {
        try {
            return Double.parseDouble(get(KEY_TAX_RATE, "0.08"));
        } catch (NumberFormatException e) {
            return 0.08;
        }
    }

    public boolean isAcceptingOrders() {
        return "true".equalsIgnoreCase(get(KEY_ACCEPTING_ORDERS, "true"));
    }

    public String getRestaurantName() {
        return get(KEY_RESTAURANT_NAME, "TastyGo Kitchen");
    }

    public String getOpeningHours() {
        return get(KEY_OPENING_HOURS, "09:00 AM - 10:00 PM");
    }
}
