package com.tastygo.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.tastygo.R;
import com.tastygo.database.FoodDao;
import com.tastygo.databinding.ActivityAddEditFoodBinding;
import com.tastygo.model.Category;
import com.tastygo.model.FoodItem;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.ImageUtil;

import java.util.List;
import java.util.Locale;

public class AddEditFoodActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD_ID = "extra_food_id";

    private ActivityAddEditFoodBinding binding;
    private FoodDao foodDao;

    private long foodId = -1;
    private String currentImagePath = "";
    private long selectedCategoryId = 1;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    saveAndPreviewImage(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        foodDao = new FoodDao(this);
        foodId = getIntent().getLongExtra(EXTRA_FOOD_ID, -1);

        setupToolbar();
        setupImagePicker();
        loadFoodData();

        binding.btnSaveFoodItem.setOnClickListener(v -> saveFoodItem());
    }

    private void setupToolbar() {
        binding.toolbarAddEditFood.setTitle(foodId > 0 ? "Edit Food Item" : "Add Food Item");
        binding.toolbarAddEditFood.setNavigationOnClickListener(v -> finish());
    }

    private void setupImagePicker() {
        binding.cardFoodImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void saveAndPreviewImage(Uri uri) {
        AppExecutor.getInstance().execute(() -> ImageUtil.saveImageToInternalStorage(this, uri), savedPath -> {
            if (savedPath != null && !savedPath.isEmpty()) {
                currentImagePath = savedPath;
                ImageUtil.loadImage(this, currentImagePath, binding.ivFoodPreview, R.drawable.ic_food_placeholder);
                binding.tvUploadPhotoLabel.setText("Change photo");
            } else {
                Snackbar.make(binding.getRoot(), "Failed to save selected image", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoodData() {
        AppExecutor.getInstance().execute(() -> {
            List<Category> categories = foodDao.getAllCategories();
            long defaultCat = (categories != null && !categories.isEmpty()) ? categories.get(0).getId() : 1L;
            FoodItem food = foodId > 0 ? foodDao.getFoodById(foodId) : null;
            return new Object[]{defaultCat, food};
        }, result -> {
            if (binding == null) return;
            Object[] data = (Object[]) result;
            long defaultCat = (long) data[0];
            FoodItem food = (FoodItem) data[1];

            if (food != null) {
                selectedCategoryId = food.getCategoryId();
                binding.etFoodName.setText(food.getName());
                binding.etFoodPrice.setText(String.format(Locale.US, "%.2f", food.getPrice()));
                binding.etFoodStock.setText(String.valueOf(food.getStock()));
                binding.etFoodDesc.setText(food.getDescription());
                binding.switchFoodAvailable.setChecked(food.isAvailable());

                currentImagePath = food.getImagePath();
                if (currentImagePath != null && !currentImagePath.isEmpty()) {
                    ImageUtil.loadImage(this, currentImagePath, binding.ivFoodPreview, R.drawable.ic_food_placeholder);
                    binding.tvUploadPhotoLabel.setText("Change photo");
                }
            } else {
                selectedCategoryId = defaultCat;
                binding.etFoodStock.setText("20");
            }
        });
    }

    private void saveFoodItem() {
        String name = binding.etFoodName.getText() != null ? binding.etFoodName.getText().toString().trim() : "";
        String priceStr = binding.etFoodPrice.getText() != null ? binding.etFoodPrice.getText().toString().trim() : "";
        String stockStr = binding.etFoodStock.getText() != null ? binding.etFoodStock.getText().toString().trim() : "";
        String desc = binding.etFoodDesc.getText() != null ? binding.etFoodDesc.getText().toString().trim() : "";
        boolean isAvailable = binding.switchFoodAvailable.isChecked();

        binding.tilFoodName.setError(null);
        binding.tilFoodPrice.setError(null);
        binding.tilFoodStock.setError(null);

        boolean hasError = false;

        if (TextUtils.isEmpty(name)) {
            binding.tilFoodName.setError(getString(R.string.error_field_required));
            hasError = true;
        }

        double price = 0.0;
        if (TextUtils.isEmpty(priceStr)) {
            binding.tilFoodPrice.setError(getString(R.string.error_field_required));
            hasError = true;
        } else {
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) {
                    binding.tilFoodPrice.setError("Price cannot be negative");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                binding.tilFoodPrice.setError("Enter a valid price");
                hasError = true;
            }
        }

        int stock = 20;
        if (TextUtils.isEmpty(stockStr)) {
            binding.tilFoodStock.setError("Enter initial stock quantity");
            hasError = true;
        } else {
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    binding.tilFoodStock.setError("Stock cannot be negative");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                binding.tilFoodStock.setError("Enter a valid number");
                hasError = true;
            }
        }

        if (hasError) return;

        binding.btnSaveFoodItem.setEnabled(false);

        final double finalPrice = price;
        final int finalStock = stock;
        final String finalImagePath = currentImagePath.isEmpty() ? "ic_food_placeholder" : currentImagePath;

        AppExecutor.getInstance().execute(() -> {
            boolean success;
            long targetFoodId = foodId;

            long catId = selectedCategoryId > 0 ? selectedCategoryId : 1L;

            if (foodId > 0) {
                // Update
                FoodItem item = new FoodItem(foodId, catId, name, desc, finalPrice, finalImagePath, isAvailable && (finalStock > 0), false, 0.0, finalStock);
                success = foodDao.updateFood(item);
            } else {
                // Insert
                FoodItem item = new FoodItem(catId, name, desc, finalPrice, finalImagePath, isAvailable && (finalStock > 0), false, 5.0, finalStock);
                targetFoodId = foodDao.insertFood(item);
                success = targetFoodId > 0;

                if (success) {
                    // Seed standard extra cheese and addon options for new food item
                    foodDao.insertOption(new com.tastygo.model.FoodOption(targetFoodId, "SIZE", "Regular Portion", 0.0));
                    foodDao.insertOption(new com.tastygo.model.FoodOption(targetFoodId, "SIZE", "Large Portion", 2.00));
                    foodDao.insertOption(new com.tastygo.model.FoodOption(targetFoodId, "ADDON", "Extra Cheddar Cheese", 0.99));
                    foodDao.insertOption(new com.tastygo.model.FoodOption(targetFoodId, "ADDON", "Melted Mozzarella", 1.25));
                    foodDao.insertOption(new com.tastygo.model.FoodOption(targetFoodId, "ADDON", "Crispy Topping Add-on", 1.49));
                }
            }

            return success;
        }, success -> {
            binding.btnSaveFoodItem.setEnabled(true);
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, foodId > 0 ? "Item updated successfully" : "Item created successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Snackbar.make(binding.getRoot(), "Failed to save item. Please try again.", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
