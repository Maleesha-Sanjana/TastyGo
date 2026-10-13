package com.tastygo.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.tastygo.database.CouponDao;
import com.tastygo.database.OrderDao;
import com.tastygo.model.CartItem;
import com.tastygo.model.Coupon;
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

    // Coupon views
    private EditText etCouponCode;
    private MaterialButton btnApplyCoupon;
    private LinearLayout layoutCouponApplied;
    private TextView tvAppliedCouponText;
    private ImageButton btnRemoveCoupon;

    // Bill summary views
    private TextView tvBillSubtotal;
    private TextView tvBillDeliveryFee;
    private TextView tvBillTax;
    private LinearLayout layoutBillDiscountRow;
    private TextView tvBillDiscount;
    private TextView tvBillTotal;
    private MaterialButton btnCheckout;

    private CartDao cartDao;
    private CouponDao couponDao;
    private OrderDao orderDao;
    private SessionManager sessionManager;
    private UserCartAdapter adapter;

    private final List<CartItem> cartItems = new ArrayList<>();
    private Coupon appliedCoupon = null;

    private double currentSubtotal = 0.0;
    private double currentDeliveryFee = 2.99;
    private double currentTax = 0.0;
    private double currentDiscount = 0.0;
    private double currentGrandTotal = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cart, container, false);

        cartDao = new CartDao(requireContext());
        couponDao = new CouponDao(requireContext());
        orderDao = new OrderDao(requireContext());
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupRecyclerView();
        setupCouponListeners();
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

        etCouponCode = view.findViewById(R.id.et_coupon_code);
        btnApplyCoupon = view.findViewById(R.id.btn_apply_coupon);
        layoutCouponApplied = view.findViewById(R.id.layout_coupon_applied);
        tvAppliedCouponText = view.findViewById(R.id.tv_applied_coupon_text);
        btnRemoveCoupon = view.findViewById(R.id.btn_remove_coupon);

        tvBillSubtotal = view.findViewById(R.id.tv_bill_subtotal);
        tvBillDeliveryFee = view.findViewById(R.id.tv_bill_delivery_fee);
        tvBillTax = view.findViewById(R.id.tv_bill_tax);
        layoutBillDiscountRow = view.findViewById(R.id.layout_bill_discount_row);
        tvBillDiscount = view.findViewById(R.id.tv_bill_discount);
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

    private void setupCouponListeners() {
        btnApplyCoupon.setOnClickListener(v -> {
            String code = etCouponCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a coupon code", Toast.LENGTH_SHORT).show();
                return;
            }

            AppExecutor.getInstance().execute(
                    () -> couponDao.validateCoupon(code, currentSubtotal),
                    coupon -> {
                        if (coupon != null) {
                            appliedCoupon = coupon;
                            layoutCouponApplied.setVisibility(View.VISIBLE);
                            tvAppliedCouponText.setText(coupon.getCode() + " applied! (" +
                                    (coupon.isPercentage() ? (int) coupon.getValue() + "% OFF" : "$" + coupon.getValue() + " OFF") + ")");
                            etCouponCode.setText("");
                            recalculateBill();
                            Toast.makeText(requireContext(), "Coupon applied successfully! 🎉", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Invalid or expired coupon code", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        btnRemoveCoupon.setOnClickListener(v -> {
            appliedCoupon = null;
            layoutCouponApplied.setVisibility(View.GONE);
            recalculateBill();
            Toast.makeText(requireContext(), "Coupon removed", Toast.LENGTH_SHORT).show();
        });
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
            order.setDiscount(currentDiscount);
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
                            appliedCoupon = null;
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
            tvBillDeliveryFee.setText(String.format(Locale.US, "$%.2f", currentDeliveryFee));
        }

        // Taxes & Charges (8%)
        currentTax = Math.round(currentSubtotal * 0.08 * 100.0) / 100.0;

        // Discount
        if (appliedCoupon != null) {
            currentDiscount = appliedCoupon.calculateDiscount(currentSubtotal);
            layoutBillDiscountRow.setVisibility(View.VISIBLE);
            tvBillDiscount.setText(String.format(Locale.US, "-$%.2f", currentDiscount));
        } else {
            currentDiscount = 0.0;
            layoutBillDiscountRow.setVisibility(View.GONE);
        }

        currentGrandTotal = Math.max(0.0, currentSubtotal - currentDiscount + currentDeliveryFee + currentTax);

        tvBillSubtotal.setText(String.format(Locale.US, "$%.2f", currentSubtotal));
        tvBillTax.setText(String.format(Locale.US, "$%.2f", currentTax));
        tvBillTotal.setText(String.format(Locale.US, "$%.2f", currentGrandTotal));
        btnCheckout.setText(String.format(Locale.US, "Proceed to Checkout • $%.2f", currentGrandTotal));
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
