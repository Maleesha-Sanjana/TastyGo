package com.tastygo.ui.user;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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

    private MaterialCardView cardAvatar;
    private EditText etSearch;
    private ImageButton btnClearSearch;
    private MaterialCardView cardHomePromo;
    private TextView tvPromoCouponCode;
    private TextView tvCategoriesTitle;
    private HorizontalScrollView scrollCategories;
    private LinearLayout layoutCategoryChips;
    private TextView tvSectionTitle;
    private TextView tvSeeAll;
    private RecyclerView rvFoods;
    private LinearLayout layoutEmpty;

    private FoodDao foodDao;
    private SessionManager sessionManager;
    private UserFoodAdapter adapter;

    private final List<FoodItem> allFoodItems = new ArrayList<>();
    private final List<Category> allCategories = new ArrayList<>();
    private String currentSearchQuery = "";
    private long selectedCategoryId = -1; // -1 means Popular now or All
    private boolean showingAllDishes = false;

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
        cardAvatar = view.findViewById(R.id.card_home_avatar);
        etSearch = view.findViewById(R.id.et_home_search);
        btnClearSearch = view.findViewById(R.id.btn_home_search_clear);
        cardHomePromo = view.findViewById(R.id.card_home_promo);
        tvPromoCouponCode = view.findViewById(R.id.tv_promo_coupon_code);
        tvCategoriesTitle = view.findViewById(R.id.tv_home_categories_title);
        scrollCategories = view.findViewById(R.id.scroll_home_categories);
        layoutCategoryChips = view.findViewById(R.id.layout_category_chips);
        tvSectionTitle = view.findViewById(R.id.tv_home_section_title);
        tvSeeAll = view.findViewById(R.id.tv_home_see_all);
        rvFoods = view.findViewById(R.id.rv_home_foods);
        layoutEmpty = view.findViewById(R.id.layout_home_empty);

        cardAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof UserMainActivity) {
                ((UserMainActivity) getActivity()).selectTab(R.id.nav_user_profile);
            }
        });

        View.OnClickListener promoCopyListener = v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("TastyGo Promo", "TASTY20");
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(requireContext(), "Coupon code TASTY20 copied! 20% off your order", Toast.LENGTH_SHORT).show();
        };

        if (cardHomePromo != null) cardHomePromo.setOnClickListener(promoCopyListener);
        if (tvPromoCouponCode != null) tvPromoCouponCode.setOnClickListener(promoCopyListener);

        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                showingAllDishes = !showingAllDishes;
                if (showingAllDishes) {
                    tvSeeAll.setText("Show Popular");
                } else {
                    tvSeeAll.setText("See all");
                }
                applyFilter();
            });
        }
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

                boolean isSearching = !currentSearchQuery.isEmpty();
                if (cardHomePromo != null) cardHomePromo.setVisibility(isSearching ? View.GONE : View.VISIBLE);
                if (tvCategoriesTitle != null) tvCategoriesTitle.setVisibility(isSearching ? View.GONE : View.VISIBLE);
                if (scrollCategories != null) scrollCategories.setVisibility(isSearching ? View.GONE : View.VISIBLE);
                if (tvSeeAll != null) tvSeeAll.setVisibility(isSearching ? View.GONE : View.VISIBLE);

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
    }

    private void loadData() {
        AppExecutor.getInstance().execute(
                () -> {
                    List<FoodItem> items = foodDao.getAllFoodItems();
                    List<Category> categories = foodDao.getAllCategories();
                    return new Object[]{items, categories};
                },
                result -> {
                    if (!isAdded()) return;
                    Object[] data = (Object[]) result;
                    List<FoodItem> items = (List<FoodItem>) data[0];
                    List<Category> categories = (List<Category>) data[1];

                    allFoodItems.clear();
                    if (items != null) {
                        allFoodItems.addAll(items);
                    }

                    allCategories.clear();
                    if (categories != null) {
                        allCategories.addAll(categories);
                    }

                    buildCategoryChips();
                    applyFilter();
                }
        );
    }

    private void buildCategoryChips() {
        if (layoutCategoryChips == null) return;
        layoutCategoryChips.removeAllViews();

        addChipView("🔥 Popular", -1);

        for (Category cat : allCategories) {
            addChipView(cat.getName(), cat.getId());
        }
    }

    private void addChipView(String label, long catId) {
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setTextSize(13);
        chip.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);

        boolean isSelected = (selectedCategoryId == catId);
        updateChipStyle(chip, isSelected);

        chip.setOnClickListener(v -> {
            selectedCategoryId = catId;
            showingAllDishes = false;
            if (tvSeeAll != null) tvSeeAll.setText("See all");
            buildCategoryChips();
            applyFilter();
        });

        layoutCategoryChips.addView(chip);
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(requireContext().getColor(R.color.white));
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(requireContext().getColor(R.color.text_ink));
            chip.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void applyFilter() {
        List<FoodItem> filtered = new ArrayList<>();
        String queryLower = currentSearchQuery.toLowerCase();

        for (FoodItem item : allFoodItems) {
            if (!item.isAvailable()) continue;

            if (!queryLower.isEmpty()) {
                boolean matchesName = item.getName() != null && item.getName().toLowerCase().contains(queryLower);
                boolean matchesDesc = item.getDescription() != null && item.getDescription().toLowerCase().contains(queryLower);
                if (!matchesName && !matchesDesc) {
                    continue;
                }
            } else if (selectedCategoryId != -1) {
                if (item.getCategoryId() != selectedCategoryId) {
                    continue;
                }
            } else if (!showingAllDishes) {
                if (!item.isPopular()) {
                    continue;
                }
            }

            filtered.add(item);
        }

        if (filtered.isEmpty() && selectedCategoryId == -1 && !showingAllDishes && queryLower.isEmpty()) {
            for (FoodItem item : allFoodItems) {
                if (item.isAvailable()) filtered.add(item);
            }
        }

        adapter.setItems(filtered);

        if (filtered.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFoods.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFoods.setVisibility(View.VISIBLE);
        }

        if (!currentSearchQuery.isEmpty()) {
            tvSectionTitle.setText("Search results (" + filtered.size() + ")");
        } else if (selectedCategoryId != -1) {
            String catName = "Category";
            for (Category c : allCategories) {
                if (c.getId() == selectedCategoryId) {
                    catName = c.getName();
                    break;
                }
            }
            tvSectionTitle.setText(catName + " (" + filtered.size() + ")");
        } else if (showingAllDishes) {
            tvSectionTitle.setText("All Dishes (" + filtered.size() + ")");
        } else {
            tvSectionTitle.setText("Popular now (" + filtered.size() + ")");
        }
    }

    @Override
    public void onFoodClick(FoodItem item) {
        Intent intent = new Intent(requireContext(), FoodDetailActivity.class);
        intent.putExtra(FoodDetailActivity.EXTRA_FOOD_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onAddClick(FoodItem item) {
        onFoodClick(item);
    }
}
