package com.tastygo.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.adapter.UserFoodAdapter;
import com.tastygo.database.FoodDao;
import com.tastygo.model.FoodItem;
import com.tastygo.ui.food.FoodDetailActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements UserFoodAdapter.OnFoodClickListener {

    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;
    private MaterialButton btnExplore;
    private TextView tvSubtitle;

    private FoodDao foodDao;
    private SessionManager sessionManager;
    private UserFoodAdapter adapter;
    private final List<FoodItem> favoriteItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_favorites, container, false);

        foodDao = new FoodDao(requireContext());
        sessionManager = new SessionManager(requireContext());

        rvFavorites = view.findViewById(R.id.rv_favorites);
        layoutEmpty = view.findViewById(R.id.layout_favorites_empty);
        btnExplore = view.findViewById(R.id.btn_fav_explore);
        tvSubtitle = view.findViewById(R.id.tv_favorites_subtitle);

        adapter = new UserFoodAdapter(requireContext(), this);
        rvFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvFavorites.setAdapter(adapter);

        btnExplore.setOnClickListener(v -> {
            if (getActivity() instanceof UserMainActivity) {
                ((UserMainActivity) getActivity()).selectTab(R.id.nav_user_home);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        long userId = sessionManager.getUserId();

        AppExecutor.getInstance().execute(
                () -> foodDao.getFavoriteFoods(userId),
                foods -> {
                    favoriteItems.clear();
                    if (foods != null) {
                        for (FoodItem item : foods) {
                            item.setFavorite(true);
                        }
                        favoriteItems.addAll(foods);
                    }
                    adapter.setItems(favoriteItems);

                    if (favoriteItems.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                        tvSubtitle.setText("No saved dishes yet");
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvFavorites.setVisibility(View.VISIBLE);
                        tvSubtitle.setText(favoriteItems.size() + " saved craving" + (favoriteItems.size() > 1 ? "s" : ""));
                    }
                }
        );
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
                    Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    loadFavorites(); // Reload to remove from the list
                }
        );
    }

    @Override
    public void onAddClick(FoodItem item) {
        onFoodClick(item);
    }
}
