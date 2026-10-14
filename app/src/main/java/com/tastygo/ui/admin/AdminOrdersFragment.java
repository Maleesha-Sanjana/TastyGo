package com.tastygo.ui.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.tastygo.R;
import com.tastygo.adapter.AdminOrderAdapter;
import com.tastygo.database.OrderDao;
import com.tastygo.model.Order;
import com.tastygo.util.AppExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrdersFragment extends Fragment implements AdminOrderAdapter.OnAdminOrderActionListener {

    private RecyclerView rvOrders;
    private TextView tvOrdersCount;
    private EditText etSearch;
    private ChipGroup chipGroupStatus;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;

    private OrderDao orderDao;
    private AdminOrderAdapter adapter;

    private final List<Order> loadedOrders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();
    private String currentStatusFilter = "ALL";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        orderDao = new OrderDao(requireContext());

        rvOrders = view.findViewById(R.id.rv_admin_orders);
        tvOrdersCount = view.findViewById(R.id.tv_admin_orders_count);
        etSearch = view.findViewById(R.id.et_admin_order_search);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        layoutEmpty = view.findViewById(R.id.layout_admin_orders_empty);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);

        adapter = new AdminOrderAdapter(requireContext(), this);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        setupFilters(view);
        setupSearch();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void setupFilters(View view) {
        view.findViewById(R.id.chip_status_all).setOnClickListener(v -> {
            currentStatusFilter = "ALL";
            loadOrders();
        });
        view.findViewById(R.id.chip_status_placed).setOnClickListener(v -> {
            currentStatusFilter = Order.STATUS_PLACED;
            loadOrders();
        });
        view.findViewById(R.id.chip_status_preparing).setOnClickListener(v -> {
            currentStatusFilter = Order.STATUS_PREPARING;
            loadOrders();
        });
        view.findViewById(R.id.chip_status_out_delivery).setOnClickListener(v -> {
            currentStatusFilter = Order.STATUS_OUT_FOR_DELIVERY;
            loadOrders();
        });
        view.findViewById(R.id.chip_status_delivered).setOnClickListener(v -> {
            currentStatusFilter = Order.STATUS_DELIVERED;
            loadOrders();
        });
        view.findViewById(R.id.chip_status_cancelled).setOnClickListener(v -> {
            currentStatusFilter = Order.STATUS_CANCELLED;
            loadOrders();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase(Locale.US) : "";
                applySearchFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadOrders() {
        AppExecutor.getInstance().execute(
                () -> orderDao.getAllOrders(currentStatusFilter),
                orders -> {
                    loadedOrders.clear();
                    if (orders != null) {
                        loadedOrders.addAll(orders);
                    }
                    applySearchFilter();
                }
        );
    }

    private void applySearchFilter() {
        filteredOrders.clear();
        if (currentSearchQuery.isEmpty()) {
            filteredOrders.addAll(loadedOrders);
        } else {
            for (Order o : loadedOrders) {
                String idStr = String.valueOf(o.getId());
                String formattedId = String.format(Locale.US, "tgo-%04d", o.getId());
                String user = o.getUserName() != null ? o.getUserName().toLowerCase(Locale.US) : "";
                String phone = o.getUserPhone() != null ? o.getUserPhone().toLowerCase(Locale.US) : "";

                if (idStr.contains(currentSearchQuery) ||
                        formattedId.contains(currentSearchQuery) ||
                        user.contains(currentSearchQuery) ||
                        phone.contains(currentSearchQuery)) {
                    filteredOrders.add(o);
                }
            }
        }

        adapter.setOrders(filteredOrders);
        tvOrdersCount.setText(filteredOrders.size() + " Order" + (filteredOrders.size() != 1 ? "s" : ""));

        if (filteredOrders.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
            if (!currentSearchQuery.isEmpty()) {
                tvEmptyTitle.setText("No Matching Orders");
                tvEmptyMessage.setText("No orders match your search \"" + currentSearchQuery + "\"");
            } else {
                tvEmptyTitle.setText("No " + (!"ALL".equalsIgnoreCase(currentStatusFilter) ? currentStatusFilter.replace("_", " ") : "") + " Orders");
                tvEmptyMessage.setText("There are currently no orders in this category.");
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUpdateStatus(Order order) {
        showStatusUpdateDialog(order);
    }

    @Override
    public void onOrderClick(Order order) {
        showStatusUpdateDialog(order);
    }

    @Override
    public void onCallCustomer(String phone) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Customer phone: " + phone, Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatusUpdateDialog(Order order) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_order_status, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_order_title);
        TextView tvSub = dialogView.findViewById(R.id.tv_dialog_order_sub);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_order_status);
        RadioButton rbPlaced = dialogView.findViewById(R.id.rb_status_placed);
        RadioButton rbPreparing = dialogView.findViewById(R.id.rb_status_preparing);
        RadioButton rbOutDelivery = dialogView.findViewById(R.id.rb_status_out_delivery);
        RadioButton rbDelivered = dialogView.findViewById(R.id.rb_status_delivered);
        RadioButton rbCancelled = dialogView.findViewById(R.id.rb_status_cancelled);
        View btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        View btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        tvTitle.setText(String.format(Locale.US, "Order #TGO-%04d", order.getId()));
        tvSub.setText("Current status: " + order.getStatus().replace("_", " "));

        String status = order.getStatus();
        if (Order.STATUS_PLACED.equalsIgnoreCase(status)) {
            rbPlaced.setChecked(true);
        } else if (Order.STATUS_PREPARING.equalsIgnoreCase(status)) {
            rbPreparing.setChecked(true);
        } else if (Order.STATUS_OUT_FOR_DELIVERY.equalsIgnoreCase(status)) {
            rbOutDelivery.setChecked(true);
        } else if (Order.STATUS_DELIVERED.equalsIgnoreCase(status)) {
            rbDelivered.setChecked(true);
        } else if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            rbCancelled.setChecked(true);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int selectedId = rgStatus.getCheckedRadioButtonId();
            String newStatus = order.getStatus();
            if (selectedId == R.id.rb_status_placed) {
                newStatus = Order.STATUS_PLACED;
            } else if (selectedId == R.id.rb_status_preparing) {
                newStatus = Order.STATUS_PREPARING;
            } else if (selectedId == R.id.rb_status_out_delivery) {
                newStatus = Order.STATUS_OUT_FOR_DELIVERY;
            } else if (selectedId == R.id.rb_status_delivered) {
                newStatus = Order.STATUS_DELIVERED;
            } else if (selectedId == R.id.rb_status_cancelled) {
                newStatus = Order.STATUS_CANCELLED;
            }

            final String updatedStatus = newStatus;
            AppExecutor.getInstance().execute(
                    () -> orderDao.updateOrderStatus(order.getId(), updatedStatus),
                    success -> {
                        dialog.dismiss();
                        if (Boolean.TRUE.equals(success)) {
                            Toast.makeText(requireContext(), "Order updated to " + updatedStatus.replace("_", " "), Toast.LENGTH_SHORT).show();
                            loadOrders();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update order status", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        dialog.show();
    }
}
