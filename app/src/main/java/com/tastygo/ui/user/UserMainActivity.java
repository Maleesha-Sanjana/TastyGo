package com.tastygo.ui.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tastygo.R;
import com.tastygo.database.CartDao;
import com.tastygo.ui.auth.LoginActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

public class UserMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;
    private CartDao cartDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        sessionManager = new SessionManager(this);
        cartDao = new CartDao(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bottomNav = findViewById(R.id.user_bottom_nav);
        setupBottomNav();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;

            if (id == R.id.nav_user_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_user_cart) {
                fragment = new CartFragment();
            } else if (id == R.id.nav_user_orders) {
                fragment = new OrdersFragment();
            } else if (id == R.id.nav_user_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    public void selectTab(int tabId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(tabId);
        }
    }

    public void updateCartBadge() {
        if (bottomNav == null) return;
        long userId = sessionManager.getUserId();

        AppExecutor.getInstance().execute(
                () -> cartDao.getCartCount(userId),
                count -> {
                    if (count > 0) {
                        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_user_cart);
                        badge.setVisible(true);
                        badge.setNumber(count);
                        badge.setBackgroundColor(getColor(R.color.primary_chili));
                    } else {
                        bottomNav.removeBadge(R.id.nav_user_cart);
                    }
                }
        );
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_fragment_container, fragment)
                .commit();
    }
}
