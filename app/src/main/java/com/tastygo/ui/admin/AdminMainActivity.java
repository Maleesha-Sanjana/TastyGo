package com.tastygo.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.tastygo.R;
import com.tastygo.databinding.ActivityAdminMainBinding;
import com.tastygo.ui.auth.LoginActivity;
import com.tastygo.ui.user.UserMainActivity;
import com.tastygo.util.SessionManager;

public class AdminMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityAdminMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Security check: only ADMIN can access
        if (!sessionManager.isAdmin()) {
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(this, UserMainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
            return;
        }

        setupDrawerAndToolbar();
        setupHeaderInfo();
        setupBackPressHandler();

        // Default to Menu Items fragment
        if (savedInstanceState == null) {
            loadFragment(new MenuItemsFragment(), "Menu Items");
            binding.adminNavView.setCheckedItem(R.id.nav_admin_menu_items);
        }
    }

    private void setupDrawerAndToolbar() {
        binding.adminToolbar.setNavigationOnClickListener(v ->
                binding.adminDrawerLayout.openDrawer(GravityCompat.START));

        binding.adminNavView.setNavigationItemSelectedListener(this);
    }

    private void setupHeaderInfo() {
        if (binding.adminNavView.getHeaderCount() > 0) {
            android.view.View header = binding.adminNavView.getHeaderView(0);
            TextView tvName = header.findViewById(R.id.tv_header_admin_name);
            TextView tvEmail = header.findViewById(R.id.tv_header_admin_email);

            if (tvName != null) tvName.setText(sessionManager.getUserName());
            if (tvEmail != null) tvEmail.setText(sessionManager.getUserEmail());
        }
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.adminDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.adminDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_admin_menu_items) {
            loadFragment(new MenuItemsFragment(), "Menu Items");
        } else if (id == R.id.nav_admin_categories) {
            loadFragment(new CategoriesFragment(), "Categories");
        } else if (id == R.id.nav_admin_dashboard) {
            Toast.makeText(this, "Admin Dashboard will be available in Phase 7", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_admin_orders) {
            Toast.makeText(this, "Order Management will be available in Phase 6", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_admin_users) {
            Toast.makeText(this, "User Management will be available in Phase 6/7", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_admin_coupons) {
            Toast.makeText(this, "Coupon Management will be available in Phase 7", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_admin_reports) {
            Toast.makeText(this, "Reports & Exports will be available in Phase 7", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_admin_settings) {
            Toast.makeText(this, "Settings will be available in Phase 8", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_admin_logout) {
            performLogout();
        }

        binding.adminDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String title) {
        binding.adminToolbar.setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }

    private void performLogout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
