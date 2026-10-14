package com.tastygo.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.tastygo.R;
import com.tastygo.ui.auth.LoginActivity;
import com.tastygo.ui.user.UserMainActivity;
import com.tastygo.util.SessionManager;

public class AdminMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout adminDrawerLayout;
    private NavigationView adminNavView;
    private MaterialToolbar adminToolbar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        adminDrawerLayout = findViewById(R.id.admin_drawer_layout);
        adminNavView = findViewById(R.id.admin_nav_view);
        adminToolbar = findViewById(R.id.admin_toolbar);

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
            adminNavView.setCheckedItem(R.id.nav_admin_menu_items);
        }
    }

    private void setupDrawerAndToolbar() {
        adminToolbar.setNavigationOnClickListener(v ->
                adminDrawerLayout.openDrawer(GravityCompat.START));

        adminNavView.setNavigationItemSelectedListener(this);
    }

    private void setupHeaderInfo() {
        if (adminNavView.getHeaderCount() > 0) {
            View header = adminNavView.getHeaderView(0);
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
                if (adminDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    adminDrawerLayout.closeDrawer(GravityCompat.START);
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
        } else if (id == R.id.nav_admin_orders) {
            loadFragment(new AdminOrdersFragment(), "Orders");
        } else if (id == R.id.nav_admin_logout) {
            performLogout();
        }

        adminDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String title) {
        adminToolbar.setTitle(title);
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
