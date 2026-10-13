package com.tastygo.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tastygo.databinding.ActivityAdminMainBinding;
import com.tastygo.ui.auth.LoginActivity;
import com.tastygo.util.SessionManager;

public class AdminMainActivity extends AppCompatActivity {

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
                startActivity(new Intent(this, com.tastygo.ui.user.UserMainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
            return;
        }

        binding.tvAdminGreeting.setText("Welcome Admin: " + sessionManager.getUserName());
        binding.tvAdminInfo.setText("Role: " + sessionManager.getUserRole() + " | Email: " + sessionManager.getUserEmail() +
                "\n\nPhase 1 Architecture initialized successfully:\n• Admin authorization verified\n• 4 Categories & 12 Foods seeded\n• Coupons & Settings ready in SQLite");

        binding.btnAdminLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
