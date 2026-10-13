package com.tastygo.ui.food;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.database.CartDao;
import com.tastygo.database.FoodDao;
import com.tastygo.model.CartItem;
import com.tastygo.model.FoodItem;
import com.tastygo.model.FoodOption;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.ImageUtil;
import com.tastygo.util.SessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FoodDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD_ID = "extra_food_id";

    private ImageView ivHero;
    private ImageButton btnBack;
    private ImageButton btnFavorite;
    private ImageView ivVegBadge;
    private TextView tvCategory;
    private TextView tvRating;
    private TextView tvTitle;
    private TextView tvBasePrice;
    private TextView tvDescription;

    private LinearLayout layoutSizesContainer;
    private RadioGroup rgSizes;
    private LinearLayout layoutAddonsContainer;
    private LinearLayout layoutAddonsList;
    private EditText etNotes;

    private ImageButton btnMinus;
    private ImageButton btnPlus;
    private TextView tvQty;
    private MaterialButton btnAddToCart;

    private FoodDao foodDao;
    private CartDao cartDao;
    private SessionManager sessionManager;

    private long foodId;
    private FoodItem foodItem;
    private final List<FoodOption> sizeOptions = new ArrayList<>();
    private final List<FoodOption> addonOptions = new ArrayList<>();
    private FoodOption selectedSize = null;
    private final Set<FoodOption> selectedAddons = new HashSet<>();
    private int quantity = 1;
    private boolean isFavorite = false;

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
        btnFavorite = findViewById(R.id.btn_detail_favorite);
        ivVegBadge = findViewById(R.id.iv_detail_veg_badge);
        tvCategory = findViewById(R.id.tv_detail_category);
        tvRating = findViewById(R.id.tv_detail_rating);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvBasePrice = findViewById(R.id.tv_detail_base_price);
        tvDescription = findViewById(R.id.tv_detail_description);

        layoutSizesContainer = findViewById(R.id.layout_sizes_container);
        rgSizes = findViewById(R.id.rg_detail_sizes);
        layoutAddonsContainer = findViewById(R.id.layout_addons_container);
        layoutAddonsList = findViewById(R.id.layout_detail_addons_list);
        etNotes = findViewById(R.id.et_detail_notes);

        btnMinus = findViewById(R.id.btn_detail_minus);
        btnPlus = findViewById(R.id.btn_detail_plus);
        tvQty = findViewById(R.id.tv_detail_qty);
        btnAddToCart = findViewById(R.id.btn_detail_add_to_cart);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFavorite.setOnClickListener(v -> {
            long userId = sessionManager.getUserId();
            AppExecutor.getInstance().execute(
                    () -> foodDao.toggleFavorite(userId, foodId),
                    favStatus -> {
                        isFavorite = favStatus;
                        updateFavoriteIcon();
                        Toast.makeText(this, isFavorite ? "Added to favorites ❤️" : "Removed from favorites", Toast.LENGTH_SHORT).show();
                    }
            );
        });

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
        long userId = sessionManager.getUserId();

        AppExecutor.getInstance().execute(
                () -> {
                    FoodItem item = foodDao.getFoodItemById(foodId);
                    List<FoodOption> options = foodDao.getOptionsForFood(foodId);
                    boolean fav = foodDao.isFavorite(userId, foodId);
                    return new Object[]{item, options, fav};
                },
                result -> {
                    foodItem = (FoodItem) ((Object[]) result)[0];
                    @SuppressWarnings("unchecked")
                    List<FoodOption> options = (List<FoodOption>) ((Object[]) result)[1];
                    isFavorite = (Boolean) ((Object[]) result)[2];

                    if (foodItem == null) {
                        Toast.makeText(this, "Item unavailable", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    bindFoodDetails(options);
                }
        );
    }

    private void bindFoodDetails(List<FoodOption> options) {
        tvTitle.setText(foodItem.getName());
        tvDescription.setText(foodItem.getDescription());
        tvBasePrice.setText(String.format(Locale.US, "$%.2f", foodItem.getPrice()));
        tvRating.setText(String.format(Locale.US, "%.1f", foodItem.getAvgRating() > 0 ? foodItem.getAvgRating() : 4.8));

        String category = foodItem.getCategoryName();
        tvCategory.setText(category != null && !category.isEmpty() ? category : "Tasty Specials");

        if (foodItem.isVeg()) {
            ivVegBadge.setImageResource(R.drawable.ic_veg);
        } else {
            ivVegBadge.setImageResource(R.drawable.ic_non_veg);
        }

        updateFavoriteIcon();
        ImageUtil.loadImage(this, foodItem.getImagePath(), ivHero, R.drawable.ic_food_placeholder);

        // Separate options
        sizeOptions.clear();
        addonOptions.clear();
        if (options != null) {
            for (FoodOption opt : options) {
                if (opt.isSize()) {
                    sizeOptions.add(opt);
                } else if (opt.isAddon()) {
                    addonOptions.add(opt);
                }
            }
        }

        populateSizeOptions();
        populateAddonOptions();
        updateTotalPrice();
    }

    private void populateSizeOptions() {
        rgSizes.removeAllViews();
        if (sizeOptions.isEmpty()) {
            layoutSizesContainer.setVisibility(View.GONE);
            selectedSize = null;
            return;
        }

        layoutSizesContainer.setVisibility(View.VISIBLE);
        for (int i = 0; i < sizeOptions.size(); i++) {
            FoodOption opt = sizeOptions.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());

            String priceText = opt.getExtraPrice() > 0 ?
                    String.format(Locale.US, " (+$%.2f)", opt.getExtraPrice()) : " (+$0.00)";
            rb.setText(opt.getName() + priceText);
            rb.setTextSize(14f);
            rb.setTextColor(getColor(R.color.text_ink));

            int index = i;
            rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedSize = sizeOptions.get(index);
                    updateTotalPrice();
                }
            });

            rgSizes.addView(rb);
            if (i == 0) {
                rb.setChecked(true);
                selectedSize = opt;
            }
        }
    }

    private void populateAddonOptions() {
        layoutAddonsList.removeAllViews();
        selectedAddons.clear();

        if (addonOptions.isEmpty()) {
            layoutAddonsContainer.setVisibility(View.GONE);
            return;
        }

        layoutAddonsContainer.setVisibility(View.VISIBLE);
        for (FoodOption opt : addonOptions) {
            CheckBox cb = new CheckBox(this);
            String priceText = opt.getExtraPrice() > 0 ?
                    String.format(Locale.US, " (+$%.2f)", opt.getExtraPrice()) : "";
            cb.setText(opt.getName() + priceText);
            cb.setTextSize(14f);
            cb.setTextColor(getColor(R.color.text_ink));

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAddons.add(opt);
                } else {
                    selectedAddons.remove(opt);
                }
                updateTotalPrice();
            });

            layoutAddonsList.addView(cb);
        }
    }

    private void updateTotalPrice() {
        if (foodItem == null) return;

        double unitPrice = foodItem.getPrice();
        if (selectedSize != null) {
            unitPrice += selectedSize.getExtraPrice();
        }
        for (FoodOption addon : selectedAddons) {
            unitPrice += addon.getExtraPrice();
        }

        double total = unitPrice * quantity;
        btnAddToCart.setText(String.format(Locale.US, "Add to Cart • $%.2f", total));
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void addToCart() {
        if (foodItem == null) return;

        long userId = sessionManager.getUserId();
        String sizeStr = selectedSize != null ? selectedSize.getName() : "";

        StringBuilder addonsSb = new StringBuilder();
        for (FoodOption opt : selectedAddons) {
            if (addonsSb.length() > 0) addonsSb.append(", ");
            addonsSb.append(opt.getName());
        }

        String note = etNotes.getText().toString().trim();

        CartItem item = new CartItem(userId, foodId, quantity, sizeStr, addonsSb.toString(), note);

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
