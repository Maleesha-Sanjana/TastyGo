package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tastygo.model.Coupon;
import com.tastygo.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for coupons table.
 */
public class CouponDao {

    private final DatabaseHelper dbHelper;

    public CouponDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<Coupon> getAllCoupons() {
        List<Coupon> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_COUPONS, null,
                null, null, null, null, "id DESC");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToCoupon(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public Coupon getCouponByCode(String code) {
        if (code == null) return null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_COUPONS, null,
                "UPPER(code) = ?", new String[]{code.trim().toUpperCase()},
                null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToCoupon(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public Coupon validateCoupon(String code, double subtotal) {
        Coupon coupon = getCouponByCode(code);
        if (coupon == null || !coupon.isActive()) {
            return null;
        }

        // Check minimum order requirement
        if (subtotal < coupon.getMinOrder()) {
            return null;
        }

        // Check expiry date if specified (format yyyy-MM-dd)
        if (coupon.getExpiryDate() != null && !coupon.getExpiryDate().isEmpty()) {
            String today = DateUtil.today();
            if (coupon.getExpiryDate().compareTo(today) < 0) {
                return null; // Expired
            }
        }

        return coupon;
    }

    public long insertCoupon(Coupon coupon) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("code", coupon.getCode().trim().toUpperCase());
        cv.put("discount_type", coupon.getDiscountType());
        cv.put("value", coupon.getValue());
        cv.put("min_order", coupon.getMinOrder());
        cv.put("expiry_date", coupon.getExpiryDate());
        cv.put("is_active", coupon.isActive() ? 1 : 0);

        return db.insert(DatabaseHelper.TABLE_COUPONS, null, cv);
    }

    public boolean updateCoupon(Coupon coupon) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("code", coupon.getCode().trim().toUpperCase());
        cv.put("discount_type", coupon.getDiscountType());
        cv.put("value", coupon.getValue());
        cv.put("min_order", coupon.getMinOrder());
        cv.put("expiry_date", coupon.getExpiryDate());
        cv.put("is_active", coupon.isActive() ? 1 : 0);

        return db.update(DatabaseHelper.TABLE_COUPONS, cv, "id = ?",
                new String[]{String.valueOf(coupon.getId())}) > 0;
    }

    public boolean toggleActive(long id, boolean active) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_active", active ? 1 : 0);
        return db.update(DatabaseHelper.TABLE_COUPONS, cv, "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteCoupon(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_COUPONS, "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    private Coupon cursorToCoupon(Cursor cursor) {
        Coupon c = new Coupon();
        c.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        c.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
        c.setDiscountType(cursor.getString(cursor.getColumnIndexOrThrow("discount_type")));
        c.setValue(cursor.getDouble(cursor.getColumnIndexOrThrow("value")));
        c.setMinOrder(cursor.getDouble(cursor.getColumnIndexOrThrow("min_order")));
        c.setExpiryDate(cursor.getString(cursor.getColumnIndexOrThrow("expiry_date")));
        c.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
        return c;
    }
}
