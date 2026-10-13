package com.tastygo.ui.admin;

import androidx.appcompat.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.tastygo.R;
import com.tastygo.adapter.FoodOptionEditAdapter;
import com.tastygo.database.FoodDao;
import com.tastygo.databinding.ActivityAddEditFoodBinding;
import com.tastygo.databinding.DialogAddOptionBinding;
import com.tastygo.model.Category;
import com.tastygo.model.FoodItem;
import com.tastygo.model.FoodOption;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddEditFoodActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD_ID = "extra_food_id";

    private ActivityAddEditFoodBinding binding;
    private FoodDao foodDao;

    private long foodId = -1;
    private String currentImagePath = "";
    private final List<Category> categoryList = new ArrayList<>();
    private long selectedCategoryId = -1;

    private FoodOptionEditAdapter sizeAdapter;
    private FoodOptionEditAdapter addonAdapter;

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
        setupAdapters();
        setupImagePicker();
        setupOptionButtons();
        loadCategoriesAndFoodData();

        binding.btnSaveFoodItem.setOnClickListener(v -> saveFoodItem());
    }

    private void setupToolbar() {
        binding.toolbarAddEditFood.setTitle(foodId > 0 ? "Edit Food Item" : "Add Food Item");
        binding.toolbarAddEditFood.setNavigationOnClickListener(v -> finish());
    }

    private void setupAdapters() {
        sizeAdapter = new FoodOptionEditAdapter((option, position) -> sizeAdapter.removeOptionAt(position));
        binding.rvSizesEdit.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSizesEdit.setAdapter(sizeAdapter);

        addonAdapter = new FoodOptionEditAdapter((option, position) -> addonAdapter.removeOptionAt(position));
        binding.rvAddonsEdit.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddonsEdit.setAdapter(addonAdapter);
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

    private void setupOptionButtons() {
        binding.btnAddSizeOption.setOnClickListener(v -> showAddOptionDialog("SIZE", "Add Size Option"));
        binding.btnAddAddonOption.setOnClickListener(v -> showAddOptionDialog("ADDON", "Add Extra / Add-on"));
    }

    private void showAddOptionDialog(String type, String dialogTitle) {
        DialogAddOptionBinding dialogBinding = DialogAddOptionBinding.inflate(getLayoutInflater());
        dialogBinding.tvDialogOptionTitle.setText(dialogTitle);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setCancelable(true)
                .create();

        dialogBinding.btnDialogOptionCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnDialogOptionAdd.setOnClickListener(v -> {
            String name = dialogBinding.etOptionName.getText() != null ?
                    dialogBinding.etOptionName.getText().toString().trim() : "";
            String priceStr = dialogBinding.etOptionExtraPrice.getText() != null ?
                    dialogBinding.etOptionExtraPrice.getText().toString().trim() : "0";

            if (TextUtils.isEmpty(name)) {
                dialogBinding.tilOptionName.setError(getString(R.string.error_field_required));
                return;
            }

            double extraPrice = 0.0;
            try {
                extraPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException ignored) {}

            FoodOption newOption = new FoodOption(foodId > 0 ? foodId : -1, type, name, extraPrice);
            if ("SIZE".equalsIgnoreCase(type)) {
                sizeAdapter.addOption(newOption);
            } else {
                addonAdapter.addOption(newOption);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadCategoriesAndFoodData() {
        AppExecutor.getInstance().execute(() -> {
            List<Category> categories = foodDao.getAllCategories();
            FoodItem food = foodId > 0 ? foodDao.getFoodById(foodId) : null;
            return new Object[]{categories, food};
        }, result -> {
            Object[] data = (Object[]) result;
            @SuppressWarnings("unchecked")
            List<Category> categories = (List<Category>) data[0];
            FoodItem food = (FoodItem) data[1];

            categoryList.clear();
            if (categories != null) categoryList.addAll(categories);

            List<String> names = new ArrayList<>();
            for (Category c : categoryList) {
                names.add(c.getName());
            }

            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            binding.actvFoodCategory.setAdapter(catAdapter);

            binding.actvFoodCategory.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < categoryList.size()) {
                    selectedCategoryId = categoryList.get(position).getId();
                }
            });

            if (food != null) {
                // Populate existing food data
                binding.etFoodName.setText(food.getName());
                binding.etFoodPrice.setText(String.format(Locale.US, "%.2f", food.getPrice()));
                binding.etFoodDesc.setText(food.getDescription());
                binding.switchFoodVeg.setChecked(food.isVeg());
                binding.switchFoodAvailable.setChecked(food.isAvailable());

                selectedCategoryId = food.getCategoryId();
                for (Category c : categoryList) {
                    if (c.getId() == selectedCategoryId) {
                        binding.actvFoodCategory.setText(c.getName(), false);
                        break;
                    }
                }

                currentImagePath = food.getImagePath();
                if (currentImagePath != null && !currentImagePath.isEmpty()) {
                    ImageUtil.loadImage(this, currentImagePath, binding.ivFoodPreview, R.drawable.ic_food_placeholder);
                    binding.tvUploadPhotoLabel.setText("Change photo");
                }

                // Populate sizes and addons
                List<FoodOption> sizes = new ArrayList<>();
                List<FoodOption> addons = new ArrayList<>();
                for (FoodOption opt : food.getOptions()) {
                    if (opt.isSize()) sizes.add(opt);
                    else addons.add(opt);
                }
                sizeAdapter.setOptions(sizes);
                addonAdapter.setOptions(addons);
            } else if (!categoryList.isEmpty()) {
                // Default first category
                selectedCategoryId = categoryList.get(0).getId();
                binding.actvFoodCategory.setText(categoryList.get(0).getName(), false);
            }
        });
    }

    private void saveFoodItem() {
        String name = binding.etFoodName.getText() != null ? binding.etFoodName.getText().toString().trim() : "";
        String priceStr = binding.etFoodPrice.getText() != null ? binding.etFoodPrice.getText().toString().trim() : "";
        String desc = binding.etFoodDesc.getText() != null ? binding.etFoodDesc.getText().toString().trim() : "";
        boolean isVeg = binding.switchFoodVeg.isChecked();
        boolean isAvailable = binding.switchFoodAvailable.isChecked();

        binding.tilFoodName.setError(null);
        binding.tilFoodPrice.setError(null);
        binding.tilFoodCategory.setError(null);

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

        if (selectedCategoryId <= 0) {
            binding.tilFoodCategory.setError("Please select a category");
            hasError = true;
        }

        if (hasError) return;

        binding.btnSaveFoodItem.setEnabled(false);

        final double finalPrice = price;
        final String finalImagePath = currentImagePath.isEmpty() ? "ic_food_placeholder" : currentImagePath;

        AppExecutor.getInstance().execute(() -> {
            boolean success;
            long targetFoodId = foodId;

            if (foodId > 0) {
                // Update
                FoodItem item = new FoodItem(foodId, selectedCategoryId, name, desc, finalPrice, finalImagePath, isAvailable, isVeg, 0.0);
                success = foodDao.updateFood(item);
                foodDao.deleteOptionsForFood(foodId);
            } else {
                // Insert
                FoodItem item = new FoodItem(selectedCategoryId, name, desc, finalPrice, finalImagePath, isAvailable, isVeg, 5.0);
                targetFoodId = foodDao.insertFood(item);
                success = targetFoodId > 0;
            }

            if (success && targetFoodId > 0) {
                // Insert sizes
                for (FoodOption opt : sizeAdapter.getOptions()) {
                    opt.setFoodId(targetFoodId);
                    foodDao.insertOption(opt);
                }
                // Insert addons
                for (FoodOption opt : addonAdapter.getOptions()) {
                    opt.setFoodId(targetFoodId);
                    foodDao.insertOption(opt);
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
