package com.tastygo.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.tastygo.adapter.AdminFoodAdapter;
import com.tastygo.database.FoodDao;
import com.tastygo.databinding.FragmentAdminMenuItemsBinding;
import com.tastygo.model.FoodItem;
import com.tastygo.util.AppExecutor;

import java.util.List;

public class MenuItemsFragment extends Fragment implements AdminFoodAdapter.OnFoodActionListener {

    private FragmentAdminMenuItemsBinding binding;
    private FoodDao foodDao;
    private AdminFoodAdapter adapter;

    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminMenuItemsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foodDao = new FoodDao(requireContext());

        setupRecyclerView();
        setupSearchAndFilters();
        setupFab();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFoods();
    }

    private void setupRecyclerView() {
        adapter = new AdminFoodAdapter(this);
        binding.rvAdminFoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminFoods.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        binding.etAdminFoodSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                binding.ivAdminFoodSearchClear.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                loadFoods();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.ivAdminFoodSearchClear.setOnClickListener(v -> binding.etAdminFoodSearch.setText(""));
    }

    private void setupFab() {
        binding.fabAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
            startActivity(intent);
        });
    }

    private void loadFoods() {
        AppExecutor.getInstance().execute(() -> foodDao.searchAndFilter(
                currentSearchQuery, null, null, null, null, "ID_DESC"
        ), foods -> {
            if (binding == null) return;

            adapter.submitList(foods);
            if (foods == null || foods.isEmpty()) {
                binding.layoutEmptyFoods.setVisibility(View.VISIBLE);
                binding.rvAdminFoods.setVisibility(View.GONE);
                binding.tvAdminItemsCount.setText("No items found");
            } else {
                binding.layoutEmptyFoods.setVisibility(View.GONE);
                binding.rvAdminFoods.setVisibility(View.VISIBLE);
                binding.tvAdminItemsCount.setText("Showing " + foods.size() + (foods.size() == 1 ? " item" : " items"));
            }
        });
    }

    @Override
    public void onEdit(FoodItem item) {
        Intent intent = new Intent(requireContext(), AddEditFoodActivity.class);
        intent.putExtra(AddEditFoodActivity.EXTRA_FOOD_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(FoodItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to remove \"" + item.getName() + "\" from the menu?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppExecutor.getInstance().execute(() -> foodDao.deleteFood(item.getId()), success -> {
                        if (Boolean.TRUE.equals(success)) {
                            Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                            loadFoods();
                        } else {
                            Snackbar.make(binding.getRoot(), "Failed to delete item", Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onAvailabilityChanged(FoodItem item, boolean isAvailable) {
        AppExecutor.getInstance().execute(() -> foodDao.setAvailability(item.getId(), isAvailable), null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
