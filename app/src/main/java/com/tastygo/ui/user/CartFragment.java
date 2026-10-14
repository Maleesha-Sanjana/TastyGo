package com.tastygo.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.adapter.UserCartAdapter;
import com.tastygo.database.CartDao;
import com.tastygo.database.OrderDao;
import com.tastygo.model.CartItem;
import com.tastygo.model.Order;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements UserCartAdapter.OnCartItemActionListener {

    private NestedScrollView scrollCartContent;
    private RecyclerView rvCartItems;
    private LinearLayout layoutCartEmpty;
    private MaterialButton btnStartShopping;
    private TextView tvCartSubtitle;

    // Bill summary views
    private TextView tvBillSubtotal;
    private TextView tvBillDeliveryFee;
    private TextView tvBillTax;
    private TextView tvBillTotal;
    private MaterialButton btnCheckout;

    private CartDao cartDao;
    private OrderDao orderDao;
    private SessionManager sessionManager;
    private UserCartAdapter adapter;

    private final List<CartItem> cartItems = new ArrayList<>();

    private double currentSubtotal = 0.0;
    private double currentDeliveryFee = 2.99;
    private double currentTax = 0.0;
    private double currentGrandTotal = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cart, container, false);

        cartDao = new CartDao(requireContext());
        orderDao = new OrderDao(requireContext());
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupRecyclerView();
        setupCheckoutListener();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void initViews(View view) {
        scrollCartContent = view.findViewById(R.id.scroll_cart_content);
        rvCartItems = view.findViewById(R.id.rv_cart_items);
        layoutCartEmpty = view.findViewById(R.id.layout_cart_empty);
        btnStartShopping = view.findViewById(R.id.btn_cart_start_shopping);
        tvCartSubtitle = view.findViewById(R.id.tv_cart_subtitle);

        tvBillSubtotal = view.findViewById(R.id.tv_bill_subtotal);
        tvBillDeliveryFee = view.findViewById(R.id.tv_bill_delivery_fee);
        tvBillTax = view.findViewById(R.id.tv_bill_tax);
        tvBillTotal = view.findViewById(R.id.tv_bill_total);
        btnCheckout = view.findViewById(R.id.btn_cart_checkout);

        btnStartShopping.setOnClickListener(v -> {
            if (getActivity() instanceof UserMainActivity) {
                ((UserMainActivity) getActivity()).selectTab(R.id.nav_user_home);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new UserCartAdapter(requireContext(), this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCartItems.setAdapter(adapter);
    }

    private void setupCheckoutListener() {
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = sessionManager.getUserId();
            Order order = new Order();
            order.setUserId(userId);
            order.setAddressId(1L); // Default Address
            order.setSubtotal(currentSubtotal);
            order.setDiscount(0.0);
            order.setDeliveryFee(currentDeliveryFee);
            order.setTax(currentTax);
            order.setTotal(currentGrandTotal);
            order.setPaymentMethod("CARD");
            order.setOrderType(Order.TYPE_DELIVERY);
            order.setStatus(Order.STATUS_PLACED);
            order.setNote("Please deliver to front door.");

            AppExecutor.getInstance().execute(
                    () -> orderDao.placeOrder(order, cartItems),
                    orderId -> {
                        if (orderId > 0) {
                            Toast.makeText(requireContext(), String.format(Locale.US, "Order #TGO-%04d placed successfully! 🚀", orderId), Toast.LENGTH_LONG).show();
                            loadCart();
                            if (getActivity() instanceof UserMainActivity) {
                                ((UserMainActivity) getActivity()).updateCartBadge();
                                ((UserMainActivity) getActivity()).selectTab(R.id.nav_user_orders);
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
    }

    private void loadCart() {
        long userId = sessionManager.getUserId();

        AppExecutor.getInstance().execute(
                () -> cartDao.getCartForUser(userId),
                items -> {
                    cartItems.clear();
                    if (items != null) {
                        cartItems.addAll(items);
                    }
                    adapter.setItems(cartItems);

                    if (cartItems.isEmpty()) {
                        layoutCartEmpty.setVisibility(View.VISIBLE);
                        scrollCartContent.setVisibility(View.GONE);
                        tvCartSubtitle.setText("0 items in cart");
                    } else {
                        layoutCartEmpty.setVisibility(View.GONE);
                        scrollCartContent.setVisibility(View.VISIBLE);
                        int count = 0;
                        for (CartItem ci : cartItems) count += ci.getQuantity();
                        tvCartSubtitle.setText(count + " delicious item" + (count > 1 ? "s" : "") + " in cart");
                    }

                    recalculateBill();
                    if (getActivity() instanceof UserMainActivity) {
                        ((UserMainActivity) getActivity()).updateCartBadge();
                    }
                }
        );
    }

    private void recalculateBill() {
        currentSubtotal = 0.0;
        for (CartItem item : cartItems) {
            currentSubtotal += item.getItemTotal();
        }

        // Delivery fee: FREE if subtotal > 30.00
        if (currentSubtotal >= 30.0 || cartItems.isEmpty()) {
            currentDeliveryFee = 0.0;
            tvBillDeliveryFee.setText("FREE");
        } else {
            currentDeliveryFee = 2.99;
            tvBillDeliveryFee.setText(String.format(Locale.US, "Rs. %.2f", currentDeliveryFee));
        }

        // Taxes & Charges (8%)
        currentTax = Math.round(currentSubtotal * 0.08 * 100.0) / 100.0;

        currentGrandTotal = Math.max(0.0, currentSubtotal + currentDeliveryFee + currentTax);

        tvBillSubtotal.setText(String.format(Locale.US, "Rs. %.2f", currentSubtotal));
        tvBillTax.setText(String.format(Locale.US, "Rs. %.2f", currentTax));
        tvBillTotal.setText(String.format(Locale.US, "Rs. %.2f", currentGrandTotal));
        btnCheckout.setText(String.format(Locale.US, "Proceed to Checkout • Rs. %.2f", currentGrandTotal));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        AppExecutor.getInstance().execute(
                () -> cartDao.updateQuantity(item.getId(), newQuantity),
                success -> loadCart()
        );
    }

    @Override
    public void onItemDeleted(CartItem item) {
        AppExecutor.getInstance().execute(
                () -> cartDao.removeCartItem(item.getId()),
                success -> {
                    Toast.makeText(requireContext(), "Item removed", Toast.LENGTH_SHORT).show();
                    loadCart();
                }
        );
    }
}
