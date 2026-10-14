package com.tastygo.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tastygo.R;
import com.tastygo.database.OrderDao;
import com.tastygo.model.Address;
import com.tastygo.ui.auth.LoginActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

public class ProfileFragment extends Fragment {

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;
    private MaterialButton btnEditAddress;
    private MaterialButton btnLogout;

    private SessionManager sessionManager;
    private OrderDao orderDao;
    private String currentAddressText = "123 Foodie Blvd, Apt 4B, New York";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        orderDao = new OrderDao(requireContext());

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvPhone = view.findViewById(R.id.tv_profile_phone);
        tvAddress = view.findViewById(R.id.tv_profile_address);
        btnEditAddress = view.findViewById(R.id.btn_profile_edit_address);
        btnLogout = view.findViewById(R.id.btn_profile_logout);

        tvName.setText(sessionManager.getUserName());
        tvEmail.setText(sessionManager.getUserEmail());
        tvPhone.setText(sessionManager.getUserPhone() != null && !sessionManager.getUserPhone().isEmpty() ?
                sessionManager.getUserPhone() : "+1 555 019 2834");

        loadUserAddress();

        btnEditAddress.setOnClickListener(v -> showEditAddressDialog());

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void loadUserAddress() {
        long userId = sessionManager.getUserId();
        AppExecutor.getInstance().execute(
                () -> orderDao.getDefaultAddress(userId),
                address -> {
                    if (address != null && !TextUtils.isEmpty(address.getAddressText())) {
                        currentAddressText = address.getAddressText();
                    }
                    tvAddress.setText(currentAddressText);
                }
        );
    }

    private void showEditAddressDialog() {
        EditText input = new EditText(requireContext());
        input.setText(currentAddressText);
        input.setSelection(currentAddressText.length());
        input.setHint("Enter delivery address");
        input.setBackgroundResource(R.drawable.bg_chip_white);
        input.setPadding(32, 24, 32, 24);

        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 48;
        params.rightMargin = 48;
        params.topMargin = 16;
        params.bottomMargin = 8;
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change Delivery Address")
                .setMessage("Update your preferred delivery location:")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newAddress = input.getText().toString().trim();
                    if (!newAddress.isEmpty()) {
                        long userId = sessionManager.getUserId();
                        AppExecutor.getInstance().execute(
                                () -> orderDao.saveOrUpdateAddress(userId, newAddress),
                                success -> {
                                    currentAddressText = newAddress;
                                    tvAddress.setText(newAddress);
                                    Toast.makeText(requireContext(), "Delivery address updated! 📍", Toast.LENGTH_SHORT).show();
                                }
                        );
                    } else {
                        Toast.makeText(requireContext(), "Address cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
