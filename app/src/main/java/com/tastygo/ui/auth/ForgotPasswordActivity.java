package com.tastygo.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.tastygo.R;
import com.tastygo.database.UserDao;
import com.tastygo.databinding.ActivityForgotPasswordBinding;
import com.tastygo.model.User;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.PasswordUtil;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private UserDao userDao;
    private long verifiedUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDao = new UserDao(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
        binding.btnVerify.setOnClickListener(v -> verifyAccount());
        binding.btnUpdatePassword.setOnClickListener(v -> updatePassword());
    }

    private void verifyAccount() {
        String email = binding.etVerifyEmail.getText() != null ? binding.etVerifyEmail.getText().toString().trim() : "";
        String phone = binding.etVerifyPhone.getText() != null ? binding.etVerifyPhone.getText().toString().trim() : "";

        binding.tilVerifyEmail.setError(null);
        binding.tilVerifyPhone.setError(null);

        boolean hasError = false;

        if (TextUtils.isEmpty(email)) {
            binding.tilVerifyEmail.setError(getString(R.string.error_field_required));
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilVerifyEmail.setError(getString(R.string.error_invalid_email));
            hasError = true;
        }

        if (TextUtils.isEmpty(phone)) {
            binding.tilVerifyPhone.setError(getString(R.string.error_field_required));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        binding.btnVerify.setEnabled(false);

        // Verification query off main thread
        AppExecutor.getInstance().execute(() -> userDao.verifyForPasswordReset(email, phone), user -> {
            binding.btnVerify.setEnabled(true);
            if (user == null) {
                Snackbar.make(binding.getRoot(), R.string.error_account_not_found, Snackbar.LENGTH_LONG).show();
            } else if (user.isBlocked()) {
                Snackbar.make(binding.getRoot(), R.string.error_user_blocked, Snackbar.LENGTH_LONG).show();
            } else {
                // Identity verified! Move to Step 2
                verifiedUserId = user.getId();
                transitionToStepTwo(user.getName());
            }
        });
    }

    private void transitionToStepTwo(String userName) {
        binding.containerStep1.setVisibility(View.GONE);
        binding.containerStep2.setVisibility(View.VISIBLE);

        binding.tvStepIndicator.setText("Step 2 of 2: Set New Password");
        binding.tvSubtitle.setText(R.string.subtitle_set_new_password);
        binding.tvVerifiedUserName.setText("Account Verified: " + userName);

        binding.etNewPassword.requestFocus();
    }

    private void updatePassword() {
        String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmNewPassword.getText() != null ? binding.etConfirmNewPassword.getText().toString().trim() : "";

        binding.tilNewPassword.setError(null);
        binding.tilConfirmNewPassword.setError(null);

        boolean hasError = false;

        if (TextUtils.isEmpty(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.error_field_required));
            hasError = true;
        } else if (newPassword.length() < 8) {
            binding.tilNewPassword.setError(getString(R.string.error_password_too_short));
            hasError = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmNewPassword.setError(getString(R.string.error_field_required));
            hasError = true;
        } else if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmNewPassword.setError(getString(R.string.error_passwords_dont_match));
            hasError = true;
        }

        if (hasError || verifiedUserId <= 0) {
            return;
        }

        binding.btnUpdatePassword.setEnabled(false);

        // Update password with newly generated salt & hash
        AppExecutor.getInstance().execute(() -> {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(newPassword, salt);
            return userDao.updatePassword(verifiedUserId, hash, salt);
        }, success -> {
            binding.btnUpdatePassword.setEnabled(true);
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.msg_password_reset_success, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Snackbar.make(binding.getRoot(), "Failed to update password. Please try again.", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
