package com.tastygo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tastygo.util.DateUtil;
import com.tastygo.util.PasswordUtil;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "tastygo.db";
    public static final int DATABASE_VERSION = 4;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_FOOD_ITEMS = "food_items";
    public static final String TABLE_FOOD_OPTIONS = "food_options";
    public static final String TABLE_CART_ITEMS = "cart_items";
    public static final String TABLE_ADDRESSES = "addresses";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String TABLE_COUPONS = "coupons";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_SETTINGS = "settings";

    private static volatile DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "role TEXT NOT NULL CHECK(role IN ('USER','ADMIN')), " +
                "is_blocked INTEGER DEFAULT 0, " +
                "created_at TEXT NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "image_path TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_FOOD_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category_id INTEGER, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "price REAL NOT NULL, " +
                "image_path TEXT, " +
                "is_available INTEGER DEFAULT 1, " +
                "is_veg INTEGER DEFAULT 1, " +
                "avg_rating REAL DEFAULT 0.0, " +
                "stock INTEGER DEFAULT 20, " +
                "is_popular INTEGER DEFAULT 0, " +
                "FOREIGN KEY(category_id) REFERENCES " + TABLE_CATEGORIES + "(id) ON DELETE SET NULL" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_FOOD_OPTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "food_id INTEGER NOT NULL, " +
                "type TEXT NOT NULL CHECK(type IN ('SIZE','ADDON')), " +
                "name TEXT NOT NULL, " +
                "extra_price REAL NOT NULL, " +
                "FOREIGN KEY(food_id) REFERENCES " + TABLE_FOOD_ITEMS + "(id) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_CART_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "food_id INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "size TEXT, " +
                "addons TEXT, " +
                "note TEXT, " +
                "unit_price REAL, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(food_id) REFERENCES " + TABLE_FOOD_ITEMS + "(id) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_ADDRESSES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "label TEXT NOT NULL, " +
                "address_text TEXT NOT NULL, " +
                "is_default INTEGER DEFAULT 0, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "address_id INTEGER, " +
                "subtotal REAL NOT NULL, " +
                "discount REAL DEFAULT 0, " +
                "delivery_fee REAL DEFAULT 0, " +
                "tax REAL DEFAULT 0, " +
                "total REAL NOT NULL, " +
                "payment_method TEXT NOT NULL, " +
                "order_type TEXT NOT NULL CHECK(order_type IN ('DELIVERY','PICKUP')), " +
                "status TEXT NOT NULL, " +
                "note TEXT, " +
                "created_at TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(address_id) REFERENCES " + TABLE_ADDRESSES + "(id) ON DELETE SET NULL" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER NOT NULL, " +
                "food_id INTEGER, " +
                "name_snapshot TEXT NOT NULL, " +
                "price_snapshot REAL NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "FOREIGN KEY(order_id) REFERENCES " + TABLE_ORDERS + "(id) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_COUPONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT UNIQUE NOT NULL, " +
                "discount_type TEXT NOT NULL CHECK(discount_type IN ('PERCENT','FLAT')), " +
                "value REAL NOT NULL, " +
                "min_order REAL DEFAULT 0, " +
                "expiry_date TEXT, " +
                "is_active INTEGER DEFAULT 1" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_REVIEWS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "food_id INTEGER NOT NULL, " +
                "rating REAL NOT NULL, " +
                "comment TEXT, " +
                "created_at TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(food_id) REFERENCES " + TABLE_FOOD_ITEMS + "(id) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" +
                "user_id INTEGER NOT NULL, " +
                "food_id INTEGER NOT NULL, " +
                "PRIMARY KEY(user_id, food_id), " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(food_id) REFERENCES " + TABLE_FOOD_ITEMS + "(id) ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                "key TEXT PRIMARY KEY, " +
                "value TEXT" +
                ");");

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_FOOD_ITEMS + " ADD COLUMN stock INTEGER DEFAULT 20");
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TABLE_CART_ITEMS + " ADD COLUMN unit_price REAL");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_FOOD_ITEMS + " ADD COLUMN is_popular INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 4) {
            applyRealPhotosAndPopularFlags(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        applyRealPhotosAndPopularFlags(db);
    }

    private void applyRealPhotosAndPopularFlags(SQLiteDatabase db) {
        try {
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET image_path = 'real_food_burger' WHERE image_path LIKE '%burger%' OR image_path LIKE '%beef%' OR image_path LIKE '%veggie%' OR image_path = 'ic_food_placeholder' OR image_path IS NULL OR image_path = ''");
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET image_path = 'real_food_pizza' WHERE image_path LIKE '%pizza%' OR image_path LIKE '%truffle%' OR image_path LIKE '%margherita%'");
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET image_path = 'real_food_ramen' WHERE image_path LIKE '%ramen%'");
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET image_path = 'real_food_padthai' WHERE image_path LIKE '%padthai%' OR image_path LIKE '%sichuan%' OR image_path LIKE '%asian%' OR image_path LIKE '%noodle%'");
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET image_path = 'real_food_smoothie' WHERE image_path LIKE '%smoothie%' OR image_path LIKE '%mango%'");
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET image_path = 'real_food_dessert' WHERE image_path LIKE '%cake%' OR image_path LIKE '%dessert%' OR image_path LIKE '%chocolate%'");
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET image_path = 'real_food_latte' WHERE image_path LIKE '%latte%' OR image_path LIKE '%coffee%' OR image_path LIKE '%drink%'");
            db.execSQL("UPDATE " + TABLE_FOOD_ITEMS + " SET is_popular = 1 WHERE name IN ('Classic Cheeseburger', 'Smoky BBQ Bacon Burger', 'Margherita Supreme', 'Pepperoni Feast', 'Iced Salted Caramel Latte')");
        } catch (Exception ignored) {}
    }

    private void seedInitialData(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            String now = DateUtil.now();

            String adminSalt = PasswordUtil.generateSalt();
            String adminHash = PasswordUtil.hashPassword("Admin@123", adminSalt);
            ContentValues adminCv = new ContentValues();
            adminCv.put("name", "TastyGo Administrator");
            adminCv.put("email", "admin@tastygo.com");
            adminCv.put("phone", "+1 234 567 8900");
            adminCv.put("password_hash", adminHash);
            adminCv.put("salt", adminSalt);
            adminCv.put("role", "ADMIN");
            adminCv.put("is_blocked", 0);
            adminCv.put("created_at", now);
            db.insert(TABLE_USERS, null, adminCv);

            String userSalt = PasswordUtil.generateSalt();
            String userHash = PasswordUtil.hashPassword("User@123", userSalt);
            ContentValues userCv = new ContentValues();
            userCv.put("name", "Alex Morgan");
            userCv.put("email", "user@tastygo.com");
            userCv.put("phone", "+1 987 654 3210");
            userCv.put("password_hash", userHash);
            userCv.put("salt", userSalt);
            userCv.put("role", "USER");
            userCv.put("is_blocked", 0);
            userCv.put("created_at", now);
            long sampleUserId = db.insert(TABLE_USERS, null, userCv);

            ContentValues addrCv = new ContentValues();
            addrCv.put("user_id", sampleUserId);
            addrCv.put("label", "Home");
            addrCv.put("address_text", "742 Evergreen Terrace, Springfield");
            addrCv.put("is_default", 1);
            db.insert(TABLE_ADDRESSES, null, addrCv);

            long catBurgers = insertCategory(db, "Burgers", "ic_cat_burger");
            long catPizza = insertCategory(db, "Pizza", "ic_cat_pizza");
            long catAsian = insertCategory(db, "Asian & Noodles", "ic_cat_asian");
            long catDrinks = insertCategory(db, "Drinks & Desserts", "ic_cat_drink");

            long f1 = insertFood(db, catBurgers, "Classic Cheeseburger",
                    "Juicy beef patty grilled to perfection with melted cheddar, crisp lettuce, pickles, and chef's signature sauce.",
                    8.99, "real_food_burger", 1, 0, 4.8, 25, 1);
            insertOption(db, f1, "SIZE", "Regular Patty", 0.0);
            insertOption(db, f1, "SIZE", "Double Patty", 2.50);
            insertOption(db, f1, "ADDON", "Extra Cheddar Cheese", 0.99);
            insertOption(db, f1, "ADDON", "Melted Mozzarella", 1.25);
            insertOption(db, f1, "ADDON", "Crispy Bacon Strips", 1.49);

            long f2 = insertFood(db, catBurgers, "Crispy Veggie Burger",
                    "House-made golden quinoa and garden vegetable patty, smashed avocado, fresh tomato, and herb aioli.",
                    7.49, "real_food_burger", 1, 1, 4.6, 20);
            insertOption(db, f2, "SIZE", "Single", 0.0);
            insertOption(db, f2, "SIZE", "Double", 2.00);
            insertOption(db, f2, "ADDON", "Extra Cheese Slice", 0.99);
            insertOption(db, f2, "ADDON", "Guacamole", 1.25);
            insertOption(db, f2, "ADDON", "Jalapeno Peppers", 0.75);

            long f3 = insertFood(db, catBurgers, "Smoky BBQ Bacon Burger",
                    "Flame-grilled prime Angus beef loaded with applewood smoked bacon, caramelized onions, and smoky BBQ glaze.",
                    9.99, "real_food_burger", 1, 0, 4.9, 18, 1);
            insertOption(db, f3, "SIZE", "Regular", 0.0);
            insertOption(db, f3, "SIZE", "Large (6oz)", 2.99);
            insertOption(db, f3, "ADDON", "Double Cheddar Cheese", 1.49);
            insertOption(db, f3, "ADDON", "Extra Smoky BBQ Sauce", 0.50);
            insertOption(db, f3, "ADDON", "Crispy Onion Rings", 1.50);

            long f4 = insertFood(db, catPizza, "Margherita Supreme",
                    "Stone-baked crust topped with San Marzano tomato sauce, fresh buffalo mozzarella, aromatic sweet basil, and extra virgin olive oil.",
                    12.99, "real_food_pizza", 1, 1, 4.7, 30, 1);
            insertOption(db, f4, "SIZE", "10 inch Medium", 0.0);
            insertOption(db, f4, "SIZE", "14 inch Large", 4.00);
            insertOption(db, f4, "ADDON", "Stuffed Cheese Crust", 2.50);
            insertOption(db, f4, "ADDON", "Extra Buffalo Mozzarella", 1.75);

            long f5 = insertFood(db, catPizza, "Pepperoni Feast",
                    "Loaded with artisanal beef and pork pepperoni, mozzarella, and crushed red pepper flakes on a crispy sourdough crust.",
                    14.99, "real_food_pizza", 1, 0, 4.9, 22, 1);
            insertOption(db, f5, "SIZE", "10 inch Medium", 0.0);
            insertOption(db, f5, "SIZE", "14 inch Large", 4.50);
            insertOption(db, f5, "ADDON", "Extra Mozzarella Melt", 1.50);
            insertOption(db, f5, "ADDON", "Stuffed Cheese Crust", 2.50);
            insertOption(db, f5, "ADDON", "Hot Honey Drizzle", 1.00);
            insertOption(db, f5, "ADDON", "Double Pepperoni", 2.25);

            long f6 = insertFood(db, catPizza, "Truffle Wild Mushroom Pizza",
                    "White cream base with roasted cremini & shiitake mushrooms, thyme, shaved parmesan, and fragrant black truffle oil.",
                    15.49, "real_food_pizza", 1, 1, 4.8, 15);
            insertOption(db, f6, "SIZE", "10 inch Medium", 0.0);
            insertOption(db, f6, "SIZE", "14 inch Large", 4.50);
            insertOption(db, f6, "ADDON", "Shaved Parmesan Cheese", 1.25);
            insertOption(db, f6, "ADDON", "Extra Truffle Drizzle", 1.75);

            long f7 = insertFood(db, catAsian, "Tonkotsu Chicken Ramen",
                    "Rich 12-hour simmered broth, springy handcrafted noodles, tender chicken chashu, ajitsuke tamago egg, and scallions.",
                    11.99, "real_food_ramen", 1, 0, 4.9, 25);
            insertOption(db, f7, "SIZE", "Standard Bowl", 0.0);
            insertOption(db, f7, "SIZE", "Jumbo Bowl", 2.50);
            insertOption(db, f7, "ADDON", "Melted Gouda Cheese", 1.25);
            insertOption(db, f7, "ADDON", "Seasoned Soft Boiled Egg", 1.25);
            insertOption(db, f7, "ADDON", "Bamboo Shoots & Nori", 1.00);

            long f8 = insertFood(db, catAsian, "Pad Thai Tofu & Peanuts",
                    "Classic stir-fried rice noodles with tamarind sauce, golden crispy tofu, bean sprouts, chives, and toasted crushed peanuts.",
                    10.49, "real_food_padthai", 1, 1, 4.6, 20);
            insertOption(db, f8, "SIZE", "Regular Portion", 0.0);
            insertOption(db, f8, "SIZE", "Family Share", 3.50);
            insertOption(db, f8, "ADDON", "Extra Crushed Peanuts", 0.75);
            insertOption(db, f8, "ADDON", "Fried Tofu Cubes", 1.50);

            long f9 = insertFood(db, catAsian, "Sichuan Chili Beef Noodles",
                    "Hand-pulled noodles tossed with slow-braised beef brisket, fragrant Sichuan chili oil, bok choy, and roasted sesame.",
                    12.99, "real_food_padthai", 1, 0, 4.7, 18);
            insertOption(db, f9, "SIZE", "Medium Spicy", 0.0);
            insertOption(db, f9, "SIZE", "Fire Spicy", 0.0);
            insertOption(db, f9, "ADDON", "Extra Braised Beef", 3.00);
            insertOption(db, f9, "ADDON", "Crispy Garlic Chili Crunch", 0.99);

            long f10 = insertFood(db, catDrinks, "Mango Passion Smoothie",
                    "Blended tropical Alphonso mango, tart passionfruit puree, organic yogurt, and crushed ice.",
                    4.99, "real_food_smoothie", 1, 1, 4.8, 40);
            insertOption(db, f10, "SIZE", "Regular (16oz)", 0.0);
            insertOption(db, f10, "SIZE", "Large (24oz)", 1.25);
            insertOption(db, f10, "ADDON", "Chia Seeds", 0.75);
            insertOption(db, f10, "ADDON", "Boba Pearls", 0.99);

            long f11 = insertFood(db, catDrinks, "Molten Chocolate Lava Cake",
                    "Warm Belgian dark chocolate cake with a luscious gooey molten core, dusted with powdered cocoa.",
                    5.99, "real_food_dessert", 1, 1, 4.9, 30);
            insertOption(db, f11, "SIZE", "Individual Serving", 0.0);
            insertOption(db, f11, "ADDON", "Madagascar Vanilla Gelato", 1.99);
            insertOption(db, f11, "ADDON", "Warm Raspberry Coulis", 0.99);

            long f12 = insertFood(db, catDrinks, "Iced Salted Caramel Latte",
                    "Double shot of espresso over cold fresh milk, house caramel drizzle, and a pinch of pink Himalayan sea salt.",
                    4.49, "real_food_latte", 1, 1, 4.7, 35, 1);
            insertOption(db, f12, "SIZE", "Medium (16oz)", 0.0);
            insertOption(db, f12, "SIZE", "Large (24oz)", 1.00);
            insertOption(db, f12, "ADDON", "Oat Milk Substitute", 0.75);
            insertOption(db, f12, "ADDON", "Extra Espresso Shot", 1.00);

            insertCoupon(db, "TASTY20", "PERCENT", 20.0, 15.00, "2028-12-31", 1);
            insertCoupon(db, "FREESHIP", "FLAT", 3.99, 20.00, "2028-12-31", 1);

            insertSetting(db, "restaurant_name", "TastyGo Kitchen");
            insertSetting(db, "opening_hours", "09:00 AM - 10:00 PM");
            insertSetting(db, "delivery_fee", "3.99");
            insertSetting(db, "tax_rate", "0.08");
            insertSetting(db, "accepting_orders", "true");

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private long insertCategory(SQLiteDatabase db, String name, String imagePath) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("image_path", imagePath);
        return db.insert(TABLE_CATEGORIES, null, cv);
    }

    private long insertFood(SQLiteDatabase db, long catId, String name, String desc,
                            double price, String imagePath, int isAvail, int isVeg, double rating, int stock) {
        return insertFood(db, catId, name, desc, price, imagePath, isAvail, isVeg, rating, stock, 0);
    }

    private long insertFood(SQLiteDatabase db, long catId, String name, String desc,
                            double price, String imagePath, int isAvail, int isVeg, double rating, int stock, int isPopular) {
        ContentValues cv = new ContentValues();
        cv.put("category_id", catId);
        cv.put("name", name);
        cv.put("description", desc);
        cv.put("price", price);
        cv.put("image_path", imagePath);
        cv.put("is_available", isAvail);
        cv.put("is_veg", isVeg);
        cv.put("avg_rating", rating);
        cv.put("stock", stock);
        cv.put("is_popular", isPopular);
        return db.insert(TABLE_FOOD_ITEMS, null, cv);
    }

    private long insertFood(SQLiteDatabase db, long catId, String name, String desc,
                            double price, String imagePath, int isAvail, int isVeg, double rating) {
        return insertFood(db, catId, name, desc, price, imagePath, isAvail, isVeg, rating, 25, 0);
    }

    private void insertOption(SQLiteDatabase db, long foodId, String type, String name, double extraPrice) {
        ContentValues cv = new ContentValues();
        cv.put("food_id", foodId);
        cv.put("type", type);
        cv.put("name", name);
        cv.put("extra_price", extraPrice);
        db.insert(TABLE_FOOD_OPTIONS, null, cv);
    }

    private void insertCoupon(SQLiteDatabase db, String code, String type, double value,
                              double minOrder, String expiry, int active) {
        ContentValues cv = new ContentValues();
        cv.put("code", code);
        cv.put("discount_type", type);
        cv.put("value", value);
        cv.put("min_order", minOrder);
        cv.put("expiry_date", expiry);
        cv.put("is_active", active);
        db.insert(TABLE_COUPONS, null, cv);
    }

    private void insertSetting(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict(TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
