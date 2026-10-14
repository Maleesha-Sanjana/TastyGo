package com.tastygo.ui.food;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.database.CartDao;
import com.tastygo.database.FoodDao;
import com.tastygo.model.CartItem;
import com.tastygo.model.FoodItem;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.ImageUtil;
import com.tastygo.util.SessionManager;

import java.util.Locale;

public class FoodDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD_ID = "extra_food_id";

    private ImageView ivHero;
    private ImageButton btnBack;
    private ImageView ivVegBadge;
    private TextView tvCategory;
    private TextView tvTitle;
    private TextView tvBasePrice;
    private TextView tvDescription;

    private ImageButton btnMinus;
    private ImageButton btnPlus;
    private TextView tvQty;
    private MaterialButton btnAddToCart;

    private FoodDao foodDao;
    private CartDao cartDao;
    private SessionManager sessionManager;

    private long foodId;
    private FoodItem foodItem;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        foodDao = new FoodDao(this);
        cartDao = new CartDao(this);
        sessionManager = new SessionManager(this);

        foodId = getIntent().getLongExtra(EXTRA_FOOD_ID, -1);
        if (foodId == -1) {
            Toast.makeText(this, "Food item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadFoodData();
    }

    private void initViews() {
        ivHero = findViewById(R.id.iv_detail_hero);
        btnBack = findViewById(R.id.btn_detail_back);
        ivVegBadge = findViewById(R.id.iv_detail_veg_badge);
        tvCategory = findViewById(R.id.tv_detail_category);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvBasePrice = findViewById(R.id.tv_detail_base_price);
        tvDescription = findViewById(R.id.tv_detail_description);

        btnMinus = findViewById(R.id.btn_detail_minus);
        btnPlus = findViewById(R.id.btn_detail_plus);
        tvQty = findViewById(R.id.tv_detail_qty);
        btnAddToCart = findViewById(R.id.btn_detail_add_to_cart);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQty.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQty.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadFoodData() {
        AppExecutor.getInstance().execute(
                () -> foodDao.getFoodItemById(foodId),
                item -> {
                    foodItem = item;
                    if (foodItem == null) {
                        Toast.makeText(this, "Item unavailable", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    bindFoodDetails();
                }
        );
    }

    private void bindFoodDetails() {
        tvTitle.setText(foodItem.getName());
        tvDescription.setText(foodItem.getDescription());
        tvBasePrice.setText(String.format(Locale.US, "Rs. %.2f", foodItem.getPrice()));

        String category = foodItem.getCategoryName();
        tvCategory.setText(category != null && !category.isEmpty() ? category : "Tasty Specials");

        if (foodItem.isVeg()) {
            ivVegBadge.setImageResource(R.drawable.ic_veg);
        } else {
            ivVegBadge.setImageResource(R.drawable.ic_non_veg);
        }

        ImageUtil.loadImage(this, foodItem.getImagePath(), ivHero, R.drawable.ic_food_placeholder);
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        if (foodItem == null) return;
        double total = foodItem.getPrice() * quantity;
        btnAddToCart.setText(String.format(Locale.US, "Buy Now • Rs. %.2f", total));
    }

    private void addToCart() {
        if (foodItem == null) return;

        long userId = sessionManager.getUserId();
        CartItem item = new CartItem(userId, foodId, quantity, "", "", "");

        AppExecutor.getInstance().execute(
                () -> cartDao.addToCart(item),
                cartItemId -> {
                    Toast.makeText(this, "Added to Cart! 🛒", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
        );
    }
}
