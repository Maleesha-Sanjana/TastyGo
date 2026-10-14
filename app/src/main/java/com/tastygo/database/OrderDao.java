package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tastygo.model.Address;
import com.tastygo.model.CartItem;
import com.tastygo.model.Order;
import com.tastygo.model.OrderItem;
import com.tastygo.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDao {

    private final DatabaseHelper dbHelper;

    public OrderDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long placeOrder(Order order, List<CartItem> cartItems) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        long orderId = -1;
        try {
            String now = DateUtil.now();

            ContentValues orderCv = new ContentValues();
            orderCv.put("user_id", order.getUserId());
            if (order.getAddressId() != null && order.getAddressId() > 0) {
                orderCv.put("address_id", order.getAddressId());
            } else {
                orderCv.putNull("address_id");
            }
            orderCv.put("subtotal", order.getSubtotal());
            orderCv.put("discount", order.getDiscount());
            orderCv.put("delivery_fee", order.getDeliveryFee());
            orderCv.put("tax", order.getTax());
            orderCv.put("total", order.getTotal());
            orderCv.put("payment_method", order.getPaymentMethod());
            orderCv.put("order_type", order.getOrderType());
            orderCv.put("status", Order.STATUS_PLACED);
            orderCv.put("note", order.getNote());
            orderCv.put("created_at", now);

            orderId = db.insertOrThrow(DatabaseHelper.TABLE_ORDERS, null, orderCv);

            for (CartItem ci : cartItems) {
                ContentValues itemCv = new ContentValues();
                itemCv.put("order_id", orderId);
                itemCv.put("food_id", ci.getFoodId());

                String nameSnapshot = ci.getFoodName();
                if (ci.getSize() != null && !ci.getSize().isEmpty()) {
                    nameSnapshot += " (" + ci.getSize() + ")";
                }
                if (ci.getAddons() != null && !ci.getAddons().isEmpty()) {
                    nameSnapshot += " + " + ci.getAddons();
                }

                itemCv.put("name_snapshot", nameSnapshot);
                itemCv.put("price_snapshot", ci.getUnitPrice());
                itemCv.put("quantity", ci.getQuantity());

                db.insertOrThrow(DatabaseHelper.TABLE_ORDER_ITEMS, null, itemCv);

                db.execSQL("UPDATE " + DatabaseHelper.TABLE_FOOD_ITEMS +
                        " SET stock = MAX(0, stock - ?), " +
                        "     is_available = CASE WHEN (stock - ?) <= 0 THEN 0 ELSE is_available END " +
                        " WHERE id = ?",
                        new Object[]{ci.getQuantity(), ci.getQuantity(), ci.getFoodId()});
            }

            db.delete(DatabaseHelper.TABLE_CART_ITEMS, "user_id = ?",
                    new String[]{String.valueOf(order.getUserId())});

            db.setTransactionSuccessful();
            return orderId;
        } finally {
            db.endTransaction();
        }
    }

    public Order getOrderById(long orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT o.*, u.name AS user_name, u.phone AS user_phone, a.address_text " +
                "FROM " + DatabaseHelper.TABLE_ORDERS + " o " +
                "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON o.user_id = u.id " +
                "LEFT JOIN " + DatabaseHelper.TABLE_ADDRESSES + " a ON o.address_id = a.id " +
                "WHERE o.id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                Order order = cursorToOrder(cursor);
                order.setItems(getOrderItems(orderId));
                return order;
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public List<OrderItem> getOrderItems(long orderId) {
        List<OrderItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ORDER_ITEMS, null,
                "order_id = ?", new String[]{String.valueOf(orderId)},
                null, null, "id ASC");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    OrderItem item = new OrderItem();
                    item.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                    item.setOrderId(cursor.getLong(cursor.getColumnIndexOrThrow("order_id")));
                    int foodIdx = cursor.getColumnIndex("food_id");
                    if (!cursor.isNull(foodIdx)) {
                        item.setFoodId(cursor.getLong(foodIdx));
                    }
                    item.setNameSnapshot(cursor.getString(cursor.getColumnIndexOrThrow("name_snapshot")));
                    item.setPriceSnapshot(cursor.getDouble(cursor.getColumnIndexOrThrow("price_snapshot")));
                    item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                    list.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<Order> getUserOrders(long userId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT o.*, u.name AS user_name, u.phone AS user_phone, a.address_text " +
                "FROM " + DatabaseHelper.TABLE_ORDERS + " o " +
                "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON o.user_id = u.id " +
                "LEFT JOIN " + DatabaseHelper.TABLE_ADDRESSES + " a ON o.address_id = a.id " +
                "WHERE o.user_id = ? " +
                "ORDER BY o.id DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = cursorToOrder(cursor);
                    order.setItems(getOrderItems(order.getId()));
                    list.add(order);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<Order> getAllOrders(String statusFilter) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT o.*, u.name AS user_name, u.phone AS user_phone, a.address_text ")
                .append("FROM ").append(DatabaseHelper.TABLE_ORDERS).append(" o ")
                .append("INNER JOIN ").append(DatabaseHelper.TABLE_USERS).append(" u ON o.user_id = u.id ")
                .append("LEFT JOIN ").append(DatabaseHelper.TABLE_ADDRESSES).append(" a ON o.address_id = a.id ");

        List<String> args = new ArrayList<>();
        if (statusFilter != null && !statusFilter.isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sb.append("WHERE o.status = ? ");
            args.add(statusFilter);
        }
        sb.append("ORDER BY o.id DESC");

        Cursor cursor = db.rawQuery(sb.toString(), args.toArray(new String[0]));
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = cursorToOrder(cursor);
                    order.setItems(getOrderItems(order.getId()));
                    list.add(order);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public boolean updateOrderStatus(long orderId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        return db.update(DatabaseHelper.TABLE_ORDERS, cv, "id = ?", new String[]{String.valueOf(orderId)}) > 0;
    }

    public boolean cancelOrder(long orderId, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Order order = getOrderById(orderId);
        if (order != null && order.getUserId() == userId && order.canBeCancelled()) {
            ContentValues cv = new ContentValues();
            cv.put("status", Order.STATUS_CANCELLED);
            return db.update(DatabaseHelper.TABLE_ORDERS, cv, "id = ?", new String[]{String.valueOf(orderId)}) > 0;
        }
        return false;
    }

    public int countOrdersToday() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String today = DateUtil.today() + "%";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE created_at LIKE ?", new String[]{today});
        try {
            if (cursor != null && cursor.moveToFirst()) return cursor.getInt(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    public double getRevenueToday() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String today = DateUtil.today() + "%";
        Cursor cursor = db.rawQuery("SELECT SUM(total) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE created_at LIKE ? AND status != 'CANCELLED'", new String[]{today});
        try {
            if (cursor != null && cursor.moveToFirst()) return cursor.getDouble(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0.0;
    }

    public double getTotalRevenue() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(total) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE status != 'CANCELLED'", null);
        try {
            if (cursor != null && cursor.moveToFirst()) return cursor.getDouble(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0.0;
    }

    public int countPendingOrders() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE status IN ('PLACED', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY')", null);
        try {
            if (cursor != null && cursor.moveToFirst()) return cursor.getInt(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    public Map<String, Float> getDailySalesForLastDays(int days) {
        Map<String, Float> sales = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT SUBSTR(created_at, 1, 10) AS day, SUM(total) AS total_sales " +
                "FROM " + DatabaseHelper.TABLE_ORDERS + " " +
                "WHERE status != 'CANCELLED' " +
                "GROUP BY day ORDER BY day DESC LIMIT ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(days)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String day = cursor.getString(0);
                    float total = cursor.getFloat(1);
                    sales.put(day, total);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return sales;
    }

    public List<Address> getUserAddresses(long userId) {
        List<Address> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ADDRESSES, null,
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "is_default DESC, id DESC");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new Address(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("label")),
                            cursor.getString(cursor.getColumnIndexOrThrow("address_text")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public Address getDefaultAddress(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ADDRESSES, null,
                "user_id = ? AND is_default = 1", new String[]{String.valueOf(userId)},
                null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return new Address(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("label")),
                        cursor.getString(cursor.getColumnIndexOrThrow("address_text")),
                        true
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        List<Address> all = getUserAddresses(userId);
        return all.isEmpty() ? null : all.get(0);
    }

    public long insertAddress(Address address) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (address.isDefault()) {
            ContentValues clearDef = new ContentValues();
            clearDef.put("is_default", 0);
            db.update(DatabaseHelper.TABLE_ADDRESSES, clearDef, "user_id = ?",
                    new String[]{String.valueOf(address.getUserId())});
        }
        ContentValues cv = new ContentValues();
        cv.put("user_id", address.getUserId());
        cv.put("label", address.getLabel());
        cv.put("address_text", address.getAddressText());
        cv.put("is_default", address.isDefault() ? 1 : 0);
        return db.insert(DatabaseHelper.TABLE_ADDRESSES, null, cv);
    }

    public boolean saveOrUpdateAddress(long userId, String addressText) {
        Address def = getDefaultAddress(userId);
        if (def != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("address_text", addressText);
            return db.update(DatabaseHelper.TABLE_ADDRESSES, cv, "id = ?", new String[]{String.valueOf(def.getId())}) > 0;
        } else {
            Address newAddr = new Address(userId, "Home", addressText, true);
            return insertAddress(newAddr) > 0;
        }
    }

    public boolean setDefaultAddress(long userId, long addressId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues clearDef = new ContentValues();
            clearDef.put("is_default", 0);
            db.update(DatabaseHelper.TABLE_ADDRESSES, clearDef, "user_id = ?", new String[]{String.valueOf(userId)});

            ContentValues setDef = new ContentValues();
            setDef.put("is_default", 1);
            int rows = db.update(DatabaseHelper.TABLE_ADDRESSES, setDef, "id = ? AND user_id = ?",
                    new String[]{String.valueOf(addressId), String.valueOf(userId)});

            db.setTransactionSuccessful();
            return rows > 0;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteAddress(long addressId, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ADDRESSES, "id = ? AND user_id = ?",
                new String[]{String.valueOf(addressId), String.valueOf(userId)}) > 0;
    }

    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        order.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        int addrIdx = cursor.getColumnIndex("address_id");
        if (!cursor.isNull(addrIdx)) {
            order.setAddressId(cursor.getLong(addrIdx));
        }
        order.setSubtotal(cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal")));
        order.setDiscount(cursor.getDouble(cursor.getColumnIndexOrThrow("discount")));
        order.setDeliveryFee(cursor.getDouble(cursor.getColumnIndexOrThrow("delivery_fee")));
        order.setTax(cursor.getDouble(cursor.getColumnIndexOrThrow("tax")));
        order.setTotal(cursor.getDouble(cursor.getColumnIndexOrThrow("total")));
        order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
        order.setOrderType(cursor.getString(cursor.getColumnIndexOrThrow("order_type")));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        order.setNote(cursor.getString(cursor.getColumnIndexOrThrow("note")));
        order.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));

        int nameIdx = cursor.getColumnIndex("user_name");
        if (nameIdx != -1) order.setUserName(cursor.getString(nameIdx));
        int phoneIdx = cursor.getColumnIndex("user_phone");
        if (phoneIdx != -1) order.setUserPhone(cursor.getString(phoneIdx));
        int textIdx = cursor.getColumnIndex("address_text");
        if (textIdx != -1) order.setAddressText(cursor.getString(textIdx));

        return order;
    }
}
