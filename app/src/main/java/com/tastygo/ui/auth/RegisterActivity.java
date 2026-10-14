package com.tastygo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.tastygo.R;
import com.tastygo.database.OrderDao;
import com.tastygo.database.UserDao;
import com.tastygo.databinding.ActivityRegisterBinding;
import com.tastygo.model.Address;
import com.tastygo.model.User;
import com.tastygo.ui.admin.AdminMainActivity;
import com.tastygo.ui.user.UserMainActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.DateUtil;
import com.tastygo.util.PasswordUtil;
import com.tastygo.util.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDao = new UserDao(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLoginPrompt.setOnClickListener(v -> finish());
        binding.btnRegister.setOnClickListener(v -> performRegistration());
    }

    private void performRegistration() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        boolean hasError = false;

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError(getString(R.string.error_field_required));
            hasError = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_field_required));
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            hasError = true;
        }

        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError(getString(R.string.error_field_required));
            hasError = true;
        } else if (phone.replaceAll("[^0-9]", "").length() < 7) {
            binding.tilPhone.setError(getString(R.string.error_invalid_phone));
            hasError = true;
        }

        if (TextUtils.isEmpty(address)) {
            binding.tilAddress.setError(getString(R.string.error_field_required));
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_field_required));
            hasError = true;
        } else if (password.length() < 8) {
            binding.tilPassword.setError(getString(R.string.error_password_too_short));
            hasError = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_field_required));
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        boolean isSeller = binding.cbSeller.isChecked();

        binding.btnRegister.setEnabled(false);

        AppExecutor.getInstance().execute(() -> {
            User existing = userDao.findByEmail(email);
            if (existing != null) {
                return -1L;
            }

            String salt = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setPasswordHash(passwordHash);
            newUser.setSalt(salt);
            newUser.setRole(isSeller ? "ADMIN" : "USER");
            newUser.setBlocked(false);
            newUser.setCreatedAt(DateUtil.now());

            long rowId = userDao.insert(newUser);
            if (rowId > 0 && !TextUtils.isEmpty(address)) {
                OrderDao orderDao = new OrderDao(RegisterActivity.this);
                orderDao.insertAddress(new Address(rowId, "Home", address, true));
            }
            return rowId;
        }, rowId -> {
            binding.btnRegister.setEnabled(true);
            if (rowId == -1L) {
                binding.tilEmail.setError(getString(R.string.error_email_already_exists));
                Snackbar.make(binding.getRoot(), R.string.error_email_already_exists, Snackbar.LENGTH_LONG).show();
            } else if (rowId > 0) {
                SessionManager sessionManager = new SessionManager(this);
                if (isSeller) {
                    sessionManager.createLoginSession(rowId, name, email, phone, "ADMIN");
                    Toast.makeText(this, "Seller account registered successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, AdminMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    sessionManager.createLoginSession(rowId, name, email, phone, "USER");
                    Toast.makeText(this, R.string.msg_register_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, UserMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            } else {
                Snackbar.make(binding.getRoot(), "Registration failed. Please try again.", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
