package com.tastygo.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.tastygo.database.DatabaseHelper;
import com.tastygo.databinding.ActivitySplashBinding;
import com.tastygo.ui.admin.AdminMainActivity;
import com.tastygo.ui.user.UserMainActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Apply saved theme preference
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        // Initialize database and seed data on background thread
        initializeAppAndRoute();
    }

    private void initializeAppAndRoute() {
        AppExecutor.getInstance().execute(() -> {
            // Force database creation and seed execution if not already done
            DatabaseHelper.getInstance(SplashActivity.this).getWritableDatabase();

            // Small delay for smooth splash presentation
            try {
                Thread.sleep(1200);
            } catch (InterruptedException ignored) {}

            return null;
        }, result -> routeNextScreen());
    }

    private void routeNextScreen() {
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isAdmin()) {
                intent = new Intent(this, AdminMainActivity.class);
            } else {
                intent = new Intent(this, UserMainActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
