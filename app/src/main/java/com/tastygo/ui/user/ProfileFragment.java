package com.tastygo.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.database.UserDao;
import com.tastygo.ui.admin.AdminMainActivity;
import com.tastygo.ui.auth.LoginActivity;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

public class ProfileFragment extends Fragment {

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;
    private MaterialButton btnSwitchAdmin;
    private MaterialButton btnLogout;

    private SessionManager sessionManager;
    private UserDao userDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        sessionManager = new SessionManager(requireContext());
        userDao = new UserDao(requireContext());

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvPhone = view.findViewById(R.id.tv_profile_phone);
        tvAddress = view.findViewById(R.id.tv_profile_address);
        btnSwitchAdmin = view.findViewById(R.id.btn_profile_switch_admin);
        btnLogout = view.findViewById(R.id.btn_profile_logout);

        tvName.setText(sessionManager.getUserName());
        tvEmail.setText(sessionManager.getUserEmail());
        tvPhone.setText(sessionManager.getUserPhone() != null && !sessionManager.getUserPhone().isEmpty() ?
                sessionManager.getUserPhone() : "+1 555 019 2834");
        tvAddress.setText("Home • 123 Foodie Blvd, Apt 4B\nDowntown, NY 10001");

        btnSwitchAdmin.setOnClickListener(v -> {
            AppExecutor.getInstance().execute(
                    () -> userDao.findByEmail("admin@tastygo.com"),
                    adminUser -> {
                        if (adminUser != null) {
                            sessionManager.createLoginSession(
                                    adminUser.getId(),
                                    adminUser.getName(),
                                    adminUser.getEmail(),
                                    adminUser.getPhone(),
                                    adminUser.getRole()
                            );
                        } else {
                            sessionManager.createLoginSession(1, "TastyGo Administrator", "admin@tastygo.com", "+1 234 567 8900", "ADMIN");
                        }
                        Toast.makeText(requireContext(), "Switched to Administrator session", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), AdminMainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
            );
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
