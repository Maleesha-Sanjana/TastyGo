package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tastygo.model.CartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for cart_items table.
 */
public class CartDao {

    private final DatabaseHelper dbHelper;

    public CartDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Retrieves all items in the user's cart joined with food item details.
     */
    public List<CartItem> getCartForUser(long userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT c.*, f.name AS food_name, f.price AS food_price, f.image_path, f.is_veg " +
                "FROM " + DatabaseHelper.TABLE_CART_ITEMS + " c " +
                "INNER JOIN " + DatabaseHelper.TABLE_FOOD_ITEMS + " f ON c.food_id = f.id " +
                "WHERE c.user_id = ? " +
                "ORDER BY c.id ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    CartItem item = new CartItem();
                    item.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                    item.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
                    item.setFoodId(cursor.getLong(cursor.getColumnIndexOrThrow("food_id")));
                    item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                    item.setSize(cursor.getString(cursor.getColumnIndexOrThrow("size")));
                    item.setAddons(cursor.getString(cursor.getColumnIndexOrThrow("addons")));
                    item.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
                    item.setFoodName(cursor.getString(cursor.getColumnIndexOrThrow("food_name")));

                    int unitPriceIdx = cursor.getColumnIndex("unit_price");
                    double unitPrice = (unitPriceIdx != -1 && !cursor.isNull(unitPriceIdx) && cursor.getDouble(unitPriceIdx) > 0)
                            ? cursor.getDouble(unitPriceIdx)
                            : cursor.getDouble(cursor.getColumnIndexOrThrow("food_price"));
                    item.setUnitPrice(unitPrice);

                    item.setFoodImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
                    item.setVeg(cursor.getInt(cursor.getColumnIndexOrThrow("is_veg")) == 1);
                    list.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * Adds an item to the cart or increments quantity if item with same configuration already exists.
     */
    public long addToCart(CartItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String sizeVal = item.getSize() != null ? item.getSize() : "";
        String addonsVal = item.getAddons() != null ? item.getAddons() : "";

        // Check for existing matching item
        Cursor cursor = db.query(DatabaseHelper.TABLE_CART_ITEMS, null,
                "user_id = ? AND food_id = ? AND size = ? AND addons = ?",
                new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getFoodId()), sizeVal, addonsVal},
                null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                long existingId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                int newQty = currentQty + item.getQuantity();

                ContentValues cv = new ContentValues();
                cv.put("quantity", newQty);
                if (item.getUnitPrice() > 0) {
                    cv.put("unit_price", item.getUnitPrice());
                }
                if (item.getNote() != null && !item.getNote().isEmpty()) {
                    cv.put("note", item.getNote());
                }
                db.update(DatabaseHelper.TABLE_CART_ITEMS, cv, "id = ?", new String[]{String.valueOf(existingId)});
                return existingId;
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        ContentValues cv = new ContentValues();
        cv.put("user_id", item.getUserId());
        cv.put("food_id", item.getFoodId());
        cv.put("quantity", item.getQuantity());
        cv.put("size", sizeVal);
        cv.put("addons", addonsVal);
        if (item.getUnitPrice() > 0) {
            cv.put("unit_price", item.getUnitPrice());
        }
        cv.put("note", item.getNote() != null ? item.getNote() : "");

        return db.insert(DatabaseHelper.TABLE_CART_ITEMS, null, cv);
    }

    /**
     * Updates item quantity or removes it if new quantity is 0 or less.
     */
    public boolean updateQuantity(long cartItemId, int newQuantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (newQuantity <= 0) {
            return db.delete(DatabaseHelper.TABLE_CART_ITEMS, "id = ?",
                    new String[]{String.valueOf(cartItemId)}) > 0;
        }
        ContentValues cv = new ContentValues();
        cv.put("quantity", newQuantity);
        return db.update(DatabaseHelper.TABLE_CART_ITEMS, cv, "id = ?",
                new String[]{String.valueOf(cartItemId)}) > 0;
    }

    /**
     * Removes an item from the cart.
     */
    public boolean removeCartItem(long cartItemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CART_ITEMS, "id = ?",
                new String[]{String.valueOf(cartItemId)}) > 0;
    }

    /**
     * Clears all items in the cart for a user.
     */
    public void clearCart(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CART_ITEMS, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    /**
     * Returns total item count in user cart.
     */
    public int getCartCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(quantity) FROM " + DatabaseHelper.TABLE_CART_ITEMS +
                " WHERE user_id = ?", new String[]{String.valueOf(userId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    /**
     * Calculates base subtotal for items in cart.
     */
    public double calculateSubtotal(long userId) {
        List<CartItem> items = getCartForUser(userId);
        double total = 0.0;
        for (CartItem item : items) {
            total += item.getItemTotal();
        }
        return total;
    }
}
