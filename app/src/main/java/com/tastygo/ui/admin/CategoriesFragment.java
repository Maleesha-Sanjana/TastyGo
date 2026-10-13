package com.tastygo.ui.admin;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.tastygo.R;
import com.tastygo.adapter.CategoryAdapter;
import com.tastygo.database.FoodDao;
import com.tastygo.databinding.DialogAddEditCategoryBinding;
import com.tastygo.databinding.FragmentAdminCategoriesBinding;
import com.tastygo.model.Category;
import com.tastygo.model.FoodItem;
import com.tastygo.util.AppExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryActionListener {

    private FragmentAdminCategoriesBinding binding;
    private FoodDao foodDao;
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foodDao = new FoodDao(requireContext());
        setupRecyclerView();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(this);
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.fabAddCategory.setOnClickListener(v -> showAddEditCategoryDialog(null));
    }

    private void loadCategories() {
        AppExecutor.getInstance().execute(() -> {
            List<Category> categories = foodDao.getAllCategories();
            List<FoodItem> foods = foodDao.getAllFoodItems();

            Map<Long, Integer> counts = new HashMap<>();
            for (FoodItem f : foods) {
                long cId = f.getCategoryId();
                counts.put(cId, counts.containsKey(cId) ? counts.get(cId) + 1 : 1);
            }

            return new Object[]{categories, counts};
        }, result -> {
            if (binding == null) return;
            Object[] data = (Object[]) result;
            @SuppressWarnings("unchecked")
            List<Category> categories = (List<Category>) data[0];
            @SuppressWarnings("unchecked")
            Map<Long, Integer> counts = (Map<Long, Integer>) data[1];

            adapter.submitList(categories);
            adapter.setItemCounts(counts);

            if (categories.isEmpty()) {
                binding.layoutEmptyCategories.setVisibility(View.VISIBLE);
                binding.rvCategories.setVisibility(View.GONE);
                binding.tvCategoryTotal.setText("No Categories");
            } else {
                binding.layoutEmptyCategories.setVisibility(View.GONE);
                binding.rvCategories.setVisibility(View.VISIBLE);
                binding.tvCategoryTotal.setText("All Categories (" + categories.size() + ")");
            }
        });
    }

    private void showAddEditCategoryDialog(@Nullable Category categoryToEdit) {
        DialogAddEditCategoryBinding dialogBinding = DialogAddEditCategoryBinding.inflate(getLayoutInflater());
        boolean isEdit = categoryToEdit != null;

        dialogBinding.tvDialogCategoryTitle.setText(isEdit ? "Edit Category" : "Add Category");
        if (isEdit) {
            dialogBinding.etCategoryName.setText(categoryToEdit.getName());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setCancelable(true)
                .create();

        dialogBinding.btnDialogCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnDialogSave.setOnClickListener(v -> {
            String name = dialogBinding.etCategoryName.getText() != null ?
                    dialogBinding.etCategoryName.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                dialogBinding.tilCategoryName.setError(getString(R.string.error_field_required));
                return;
            }

            dialogBinding.tilCategoryName.setError(null);
            dialog.dismiss();

            AppExecutor.getInstance().execute(() -> {
                if (isEdit) {
                    categoryToEdit.setName(name);
                    return foodDao.updateCategory(categoryToEdit);
                } else {
                    Category newCat = new Category(name, "ic_category");
                    return foodDao.insertCategory(newCat) > 0;
                }
            }, success -> {
                if (Boolean.TRUE.equals(success)) {
                    Toast.makeText(requireContext(), isEdit ? "Category updated" : "Category added", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    Snackbar.make(binding.getRoot(), "Failed to save category", Snackbar.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onEdit(Category category) {
        showAddEditCategoryDialog(category);
    }

    @Override
    public void onDelete(Category category) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete \"" + category.getName() + "\"? Associated menu items will remain intact without a category.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppExecutor.getInstance().execute(() -> foodDao.deleteCategory(category.getId()), success -> {
                        if (Boolean.TRUE.equals(success)) {
                            Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        } else {
                            Snackbar.make(binding.getRoot(), "Failed to delete category", Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
