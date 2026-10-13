package com.tastygo.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.tastygo.R;
import com.tastygo.adapter.UserFoodAdapter;
import com.tastygo.database.FoodDao;
import com.tastygo.model.Category;
import com.tastygo.model.FoodItem;
import com.tastygo.ui.food.FoodDetailActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements UserFoodAdapter.OnFoodClickListener {

    private TextView tvAddress;
    private MaterialCardView cardAvatar;
    private EditText etSearch;
    private ImageButton btnClearSearch;
    private ChipGroup chipGroupCategories;
    private Chip chipVegOnly;
    private RecyclerView rvFoods;
    private LinearLayout layoutEmpty;
    private TextView tvSectionTitle;

    private FoodDao foodDao;
    private SessionManager sessionManager;
    private UserFoodAdapter adapter;

    private final List<FoodItem> allFoodItems = new ArrayList<>();
    private final List<Category> allCategories = new ArrayList<>();
    private long selectedCategoryId = -1; // -1 means All
    private boolean isVegOnly = false;
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_home, container, false);

        foodDao = new FoodDao(requireContext());
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupRecyclerView();
        setupFiltersAndSearch();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        tvAddress = view.findViewById(R.id.tv_home_address);
        cardAvatar = view.findViewById(R.id.card_home_avatar);
        etSearch = view.findViewById(R.id.et_home_search);
        btnClearSearch = view.findViewById(R.id.btn_home_search_clear);
        chipGroupCategories = view.findViewById(R.id.chip_group_home_categories);
        chipVegOnly = view.findViewById(R.id.chip_home_veg_only);
        rvFoods = view.findViewById(R.id.rv_home_foods);
        layoutEmpty = view.findViewById(R.id.layout_home_empty);
        tvSectionTitle = view.findViewById(R.id.tv_home_section_title);

        tvAddress.setText("123 Foodie Blvd, Apt 4B");

        cardAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof UserMainActivity) {
                ((UserMainActivity) getActivity()).selectTab(R.id.nav_user_profile);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new UserFoodAdapter(requireContext(), this);
        rvFoods.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvFoods.setAdapter(adapter);
    }

    private void setupFiltersAndSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim() : "";
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentSearchQuery = "";
            applyFilter();
        });

        chipVegOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVegOnly = isChecked;
            applyFilter();
        });
    }

    private void loadData() {
        long userId = sessionManager.getUserId();

        AppExecutor.getInstance().execute(
                () -> {
                    List<FoodItem> items = foodDao.getAllFoodItems();
                    List<Category> categories = foodDao.getAllCategories();
                    // Set favorite flags
                    for (FoodItem item : items) {
                        item.setFavorite(foodDao.isFavorite(userId, item.getId()));
                    }
                    return new Object[]{items, categories};
                },
                result -> {
                    allFoodItems.clear();
                    @SuppressWarnings("unchecked")
                    List<FoodItem> items = (List<FoodItem>) ((Object[]) result)[0];
                    allFoodItems.addAll(items);

                    allCategories.clear();
                    @SuppressWarnings("unchecked")
                    List<Category> categories = (List<Category>) ((Object[]) result)[1];
                    allCategories.addAll(categories);

                    populateCategoryChips();
                    applyFilter();
                }
        );
    }

    private void populateCategoryChips() {
        if (!isAdded()) return;

        // Keep "All" chip, add rest
        chipGroupCategories.removeAllViews();

        Chip allChip = new Chip(requireContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(selectedCategoryId == -1);
        allChip.setOnClickListener(v -> {
            selectedCategoryId = -1;
            applyFilter();
        });
        chipGroupCategories.addView(allChip);

        for (Category cat : allCategories) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat.getName());
            chip.setCheckable(true);
            chip.setChecked(selectedCategoryId == cat.getId());
            chip.setOnClickListener(v -> {
                selectedCategoryId = cat.getId();
                applyFilter();
            });
            chipGroupCategories.addView(chip);
        }
    }

    private void applyFilter() {
        List<FoodItem> filtered = new ArrayList<>();
        String queryLower = currentSearchQuery.toLowerCase();

        for (FoodItem item : allFoodItems) {
            // Check availability (only available foods shown to customers)
            if (!item.isAvailable()) continue;

            // Category filter
            if (selectedCategoryId != -1 && item.getCategoryId() != selectedCategoryId) {
                continue;
            }

            // Veg only filter
            if (isVegOnly && !item.isVeg()) {
                continue;
            }

            // Search query filter
            if (!queryLower.isEmpty()) {
                boolean matchesName = item.getName() != null && item.getName().toLowerCase().contains(queryLower);
                boolean matchesDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(queryLower);
                boolean matchesCat = item.getCategoryName() != null && item.getCategoryName().toLowerCase().contains(queryLower);
                if (!matchesName && !matchesDesc && !matchesCat) {
                    continue;
                }
            }

            filtered.add(item);
        }

        adapter.setItems(filtered);

        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFoods.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFoods.setVisibility(View.VISIBLE);
        }

        tvSectionTitle.setText("Popular Dishes (" + filtered.size() + ")");
    }

    @Override
    public void onFoodClick(FoodItem item) {
        Intent intent = new Intent(requireContext(), FoodDetailActivity.class);
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(FoodItem item, int position) {
        long userId = sessionManager.getUserId();
        AppExecutor.getInstance().execute(
                () -> foodDao.toggleFavorite(userId, item.getId()),
                fav -> {
                    item.setFavorite(fav);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(requireContext(),
                            fav ? "Added to favorites ❤️" : "Removed from favorites",
                            Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void onAddClick(FoodItem item) {
        onFoodClick(item);
    }
}
