package com.tastygo.ui.food;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.android.material.card.MaterialCardView;
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
import java.util.List;
import java.util.Locale;

public class FoodDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD_ID = "extra_food_id";

    private ImageView ivHero;
    private ImageButton btnBack;
    private ImageView ivVegBadge;
    private TextView tvCategory;
    private TextView tvStock;
    private TextView tvTitle;
    private TextView tvBasePrice;
    private TextView tvDescription;

    // Options Sections
    private MaterialCardView cardSectionSize;
    private RadioGroup rgSizes;
    private MaterialCardView cardSectionCheese;
    private LinearLayout layoutCheeseOptions;
    private MaterialCardView cardSectionAddons;
    private LinearLayout layoutAddonOptions;
    private EditText etNote;

    // Stepper & Add Button
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

    // Selections
    private FoodOption selectedSize = null;
    private final List<FoodOption> selectedCheeses = new ArrayList<>();
    private final List<FoodOption> selectedAddons = new ArrayList<>();

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
        tvStock = findViewById(R.id.tv_detail_stock);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvBasePrice = findViewById(R.id.tv_detail_base_price);
        tvDescription = findViewById(R.id.tv_detail_description);

        cardSectionSize = findViewById(R.id.card_section_size);
        rgSizes = findViewById(R.id.rg_sizes);
        cardSectionCheese = findViewById(R.id.card_section_cheese);
        layoutCheeseOptions = findViewById(R.id.layout_cheese_options);
        cardSectionAddons = findViewById(R.id.card_section_addons);
        layoutAddonOptions = findViewById(R.id.layout_addon_options);
        etNote = findViewById(R.id.et_detail_note);

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
            if (foodItem == null) return;
            if (quantity < foodItem.getStock()) {
                quantity++;
                tvQty.setText(String.valueOf(quantity));
                updateTotalPrice();
            } else {
                Toast.makeText(this, "Only " + foodItem.getStock() + " available in stock", Toast.LENGTH_SHORT).show();
            }
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
                    bindOptions();
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

        // Stock status
        int stock = foodItem.getStock();
        boolean inStock = foodItem.isInStock();

        if (!inStock || stock <= 0) {
            tvStock.setText("Out of Stock");
            tvStock.setTextColor(getColor(R.color.non_veg_red));
            tvStock.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.status_cancelled_bg)));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText("Out of Stock");
            btnPlus.setEnabled(false);
            btnMinus.setEnabled(false);
        } else {
            if (stock <= 5) {
                tvStock.setText("Low Stock: Only " + stock + " left!");
                tvStock.setTextColor(getColor(R.color.text_ink));
                tvStock.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.status_preparing_bg)));
            } else {
                tvStock.setText("In Stock (" + stock + " left)");
                tvStock.setTextColor(getColor(R.color.success_basil));
                tvStock.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.status_delivered_bg)));
            }
            btnAddToCart.setEnabled(true);
            btnPlus.setEnabled(true);
            btnMinus.setEnabled(true);
        }

        ImageUtil.loadImage(this, foodItem.getImagePath(), ivHero, R.drawable.ic_food_placeholder);
        updateTotalPrice();
    }

    private void bindOptions() {
        List<FoodOption> options = foodItem.getOptions();

        // If no options configured in DB, provide contextual defaults
        if (options == null || options.isEmpty()) {
            options = getDefaultOptionsForFood(foodItem);
        }

        List<FoodOption> sizeOptions = new ArrayList<>();
        List<FoodOption> cheeseOptions = new ArrayList<>();
        List<FoodOption> addonOptions = new ArrayList<>();

        for (FoodOption opt : options) {
            if (opt.isSize()) {
                sizeOptions.add(opt);
            } else if (opt.isCheese()) {
                cheeseOptions.add(opt);
            } else {
                addonOptions.add(opt);
            }
        }

        // Always ensure at least 2 cheese choices if cheese options were empty
        if (cheeseOptions.isEmpty()) {
            cheeseOptions.add(new FoodOption(foodId, "ADDON", "Extra Cheddar Cheese", 0.99));
            cheeseOptions.add(new FoodOption(foodId, "ADDON", "Melted Mozzarella", 1.25));
        }

        // Always ensure delicious add-ons if addon options were empty
        if (addonOptions.isEmpty()) {
            addonOptions.add(new FoodOption(foodId, "ADDON", "Crispy Bacon Strips", 1.49));
            addonOptions.add(new FoodOption(foodId, "ADDON", "Caramelized Onions", 0.75));
        }

        // 1. Setup Sizes
        if (!sizeOptions.isEmpty()) {
            cardSectionSize.setVisibility(View.VISIBLE);
            rgSizes.removeAllViews();
            for (int i = 0; i < sizeOptions.size(); i++) {
                FoodOption sizeOpt = sizeOptions.get(i);
                RadioButton rb = new RadioButton(this);
                rb.setId(View.generateViewId());
                String label = sizeOpt.getName();
                if (sizeOpt.getExtraPrice() > 0) {
                    label += String.format(Locale.US, " (+Rs. %.2f)", sizeOpt.getExtraPrice());
                }
                rb.setText(label);
                rb.setTextColor(getColor(R.color.text_ink));
                rb.setTextSize(14f);
                rb.setButtonTintList(ColorStateList.valueOf(getColor(R.color.primary_chili)));
                rb.setPadding(8, 12, 8, 12);

                rgSizes.addView(rb);

                if (i == 0) {
                    rb.setChecked(true);
                    selectedSize = sizeOpt;
                }

                final FoodOption currentOpt = sizeOpt;
                rb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedSize = currentOpt;
                        updateTotalPrice();
                    }
                });
            }
        } else {
            cardSectionSize.setVisibility(View.GONE);
            selectedSize = null;
        }

        // 2. Setup Extra Cheese Section
        cardSectionCheese.setVisibility(View.VISIBLE);
        layoutCheeseOptions.removeAllViews();
        selectedCheeses.clear();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (FoodOption cheeseOpt : cheeseOptions) {
            View itemView = inflater.inflate(R.layout.item_food_option_choice, layoutCheeseOptions, false);
            CheckBox cb = itemView.findViewById(R.id.cb_option);
            TextView tvName = itemView.findViewById(R.id.tv_option_name);
            TextView tvPrice = itemView.findViewById(R.id.tv_option_price);

            tvName.setText(cheeseOpt.getName());
            tvPrice.setText(String.format(Locale.US, "+Rs. %.2f", cheeseOpt.getExtraPrice()));

            itemView.setOnClickListener(v -> cb.setChecked(!cb.isChecked()));

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCheeses.add(cheeseOpt);
                } else {
                    selectedCheeses.remove(cheeseOpt);
                }
                updateTotalPrice();
            });

            layoutCheeseOptions.addView(itemView);
        }

        // 3. Setup Add-ons & Extras Section
        cardSectionAddons.setVisibility(View.VISIBLE);
        layoutAddonOptions.removeAllViews();
        selectedAddons.clear();

        for (FoodOption addonOpt : addonOptions) {
            View itemView = inflater.inflate(R.layout.item_food_option_choice, layoutAddonOptions, false);
            CheckBox cb = itemView.findViewById(R.id.cb_option);
            TextView tvName = itemView.findViewById(R.id.tv_option_name);
            TextView tvPrice = itemView.findViewById(R.id.tv_option_price);

            tvName.setText(addonOpt.getName());
            tvPrice.setText(String.format(Locale.US, "+Rs. %.2f", addonOpt.getExtraPrice()));

            itemView.setOnClickListener(v -> cb.setChecked(!cb.isChecked()));

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAddons.add(addonOpt);
                } else {
                    selectedAddons.remove(addonOpt);
                }
                updateTotalPrice();
            });

            layoutAddonOptions.addView(itemView);
        }

        updateTotalPrice();
    }

    private List<FoodOption> getDefaultOptionsForFood(FoodItem item) {
        List<FoodOption> defaults = new ArrayList<>();
        defaults.add(new FoodOption(item.getId(), "SIZE", "Regular", 0.0));
        defaults.add(new FoodOption(item.getId(), "SIZE", "Large", 2.50));
        defaults.add(new FoodOption(item.getId(), "ADDON", "Extra Cheddar Cheese", 0.99));
        defaults.add(new FoodOption(item.getId(), "ADDON", "Melted Mozzarella", 1.25));
        defaults.add(new FoodOption(item.getId(), "ADDON", "Crispy Bacon Strips", 1.49));
        defaults.add(new FoodOption(item.getId(), "ADDON", "Jalapeno Peppers", 0.75));
        return defaults;
    }

    private double calculateCalculatedUnitPrice() {
        if (foodItem == null) return 0.0;
        double price = foodItem.getPrice();
        if (selectedSize != null) {
            price += selectedSize.getExtraPrice();
        }
        for (FoodOption cheese : selectedCheeses) {
            price += cheese.getExtraPrice();
        }
        for (FoodOption addon : selectedAddons) {
            price += addon.getExtraPrice();
        }
        return price;
    }

    private void updateTotalPrice() {
        if (foodItem == null) return;
        if (!foodItem.isInStock()) {
            btnAddToCart.setText("Out of Stock");
            return;
        }
        double unitPrice = calculateCalculatedUnitPrice();
        double grandTotal = unitPrice * quantity;
        btnAddToCart.setText(String.format(Locale.US, "Add to Cart • Rs. %.2f", grandTotal));
    }

    private void addToCart() {
        if (foodItem == null) return;
        if (!foodItem.isInStock()) {
            Toast.makeText(this, "Item is currently out of stock", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = sessionManager.getUserId();
        String sizeName = selectedSize != null ? selectedSize.getName() : "";

        // Combine selected cheese and addon labels
        List<String> combinedAddons = new ArrayList<>();
        for (FoodOption fo : selectedCheeses) {
            combinedAddons.add(fo.getName());
        }
        for (FoodOption fo : selectedAddons) {
            combinedAddons.add(fo.getName());
        }
        String addonsString = android.text.TextUtils.join(", ", combinedAddons);

        String note = etNote != null && etNote.getText() != null ? etNote.getText().toString().trim() : "";
        double customizedUnitPrice = calculateCalculatedUnitPrice();

        CartItem item = new CartItem(userId, foodId, quantity, sizeName, addonsString, note);
        item.setUnitPrice(customizedUnitPrice);

        AppExecutor.getInstance().execute(
                () -> cartDao.addToCart(item),
                cartItemId -> {
                    Toast.makeText(this, "Added to Cart with customizations! 🛒", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
        );
    }
}
