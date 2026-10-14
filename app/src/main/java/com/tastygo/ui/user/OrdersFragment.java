package com.tastygo.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.adapter.UserOrderAdapter;
import com.tastygo.database.OrderDao;
import com.tastygo.model.Order;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private MaterialButton btnBrowse;
    private TextView tvSubtitle;

    private OrderDao orderDao;
    private SessionManager sessionManager;
    private UserOrderAdapter adapter;
    private final List<Order> userOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_orders, container, false);

        orderDao = new OrderDao(requireContext());
        sessionManager = new SessionManager(requireContext());

        rvOrders = view.findViewById(R.id.rv_orders);
        layoutEmpty = view.findViewById(R.id.layout_orders_empty);
        btnBrowse = view.findViewById(R.id.btn_orders_browse);
        tvSubtitle = view.findViewById(R.id.tv_orders_subtitle);

        adapter = new UserOrderAdapter(requireContext());
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);

        btnBrowse.setOnClickListener(v -> {
            if (getActivity() instanceof UserMainActivity) {
                ((UserMainActivity) getActivity()).selectTab(R.id.nav_user_home);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        long userId = sessionManager.getUserId();

        AppExecutor.getInstance().execute(
                () -> orderDao.getUserOrders(userId),
                orders -> {
                    userOrders.clear();
                    if (orders != null) {
                        userOrders.addAll(orders);
                    }
                    adapter.setOrders(userOrders);

                    if (userOrders.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvOrders.setVisibility(View.GONE);
                        tvSubtitle.setText("0 orders placed");
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);
                        tvSubtitle.setText(userOrders.size() + " total order" + (userOrders.size() > 1 ? "s" : ""));
                    }
                }
        );
    }
}
