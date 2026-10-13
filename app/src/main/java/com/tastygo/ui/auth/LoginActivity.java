package com.tastygo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.tastygo.R;
import com.tastygo.database.UserDao;
import com.tastygo.databinding.ActivityLoginBinding;
import com.tastygo.model.User;
import com.tastygo.ui.admin.AdminMainActivity;
import com.tastygo.ui.user.UserMainActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.PasswordUtil;
import com.tastygo.util.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.tvRegisterPrompt.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Reset password will be available in Phase 2", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        // Validations
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_field_required));
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_field_required));
            return;
        } else if (password.length() < 8) {
            binding.tilPassword.setError(getString(R.string.error_password_too_short));
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        binding.btnLogin.setEnabled(false);

        // Perform authentication off main thread
        AppExecutor.getInstance().execute(() -> {
            User user = userDao.findByEmail(email);
            if (user == null) {
                return null;
            }
            boolean matches = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
            if (!matches) {
                return null;
            }
            return user;
        }, user -> {
            binding.btnLogin.setEnabled(true);
            if (user == null) {
                Snackbar.make(binding.getRoot(), R.string.error_invalid_credentials, Snackbar.LENGTH_LONG).show();
                return;
            }

            if (user.isBlocked()) {
                Snackbar.make(binding.getRoot(), R.string.error_user_blocked, Snackbar.LENGTH_LONG).show();
                return;
            }

            // Save session
            sessionManager.createLoginSession(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());

            // Route by role
            Intent intent;
            if (user.isAdmin()) {
                intent = new Intent(this, AdminMainActivity.class);
            } else {
                intent = new Intent(this, UserMainActivity.class);
            }
            startActivity(intent);
            finish();
        });
    }
}
