package com.tastygo.ui.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tastygo.databinding.ActivityUserMainBinding;
import com.tastygo.ui.auth.LoginActivity;
import com.tastygo.util.SessionManager;

public class UserMainActivity extends AppCompatActivity {

    private ActivityUserMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.tvUserGreeting.setText("Welcome, " + sessionManager.getUserName() + "!");
        binding.tvUserInfo.setText("Role: " + sessionManager.getUserRole() + " | Email: " + sessionManager.getUserEmail() +
                "\n\nPhase 1 Architecture initialized successfully:\n• SQLite Helper & DAOs connected\n• Seed data loaded\n• Session tracking active");

        binding.btnUserLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
