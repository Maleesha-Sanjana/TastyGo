package com.tastygo.ui.user;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.tastygo.R;
import com.tastygo.database.OrderDao;
import com.tastygo.database.ReviewDao;
import com.tastygo.model.Order;
import com.tastygo.model.OrderItem;
import com.tastygo.model.Review;
import com.tastygo.util.AppExecutor;
import com.tastygo.util.DateUtil;
import com.tastygo.util.SessionManager;

import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private MaterialToolbar toolbar;
    private TextView tvOrderId;
    private TextView tvStatusBadge;
    private TextView tvDateTime;

    // Tracker views
    private MaterialCardView cardTracker;
    private TextView tvTrackerSubtitle;
    private LinearLayout layoutStepper;
    private ImageView ivStepPlaced;
    private TextView tvStepPlaced;
    private View lineStep12;
    private ImageView ivStepPreparing;
    private TextView tvStepPreparing;
    private View lineStep23;
    private ImageView ivStepOut;
    private TextView tvStepOut;
    private View lineStep34;
    private ImageView ivStepDelivered;
    private TextView tvStepDelivered;
    private LinearLayout layoutCancelledBanner;

    // Delivery info
    private TextView tvAddress;
    private TextView tvPhone;
    private LinearLayout layoutPhone;
    private TextView tvPaymentInfo;
    private TextView tvNote;

    // Items
    private LinearLayout layoutItemsContainer;

    // Bill
    private TextView tvSubtotal;
    private TextView tvDeliveryFee;
    private TextView tvTax;
    private LinearLayout layoutDiscount;
    private TextView tvDiscount;
    private TextView tvGrandTotal;

    // Actions
    private MaterialButton btnLeaveReview;
    private MaterialButton btnCancelOrder;

    private OrderDao orderDao;
    private ReviewDao reviewDao;
    private SessionManager sessionManager;
    private long orderId = -1;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        if (orderId <= 0) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderDao = new OrderDao(this);
        reviewDao = new ReviewDao(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupToolbar();
        loadOrderDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_order_detail);
        tvOrderId = findViewById(R.id.tv_detail_order_id);
        tvStatusBadge = findViewById(R.id.tv_detail_status_badge);
        tvDateTime = findViewById(R.id.tv_detail_date_time);

        cardTracker = findViewById(R.id.card_tracker);
        tvTrackerSubtitle = findViewById(R.id.tv_tracker_subtitle);
        layoutStepper = findViewById(R.id.layout_stepper);
        ivStepPlaced = findViewById(R.id.iv_step_placed);
        tvStepPlaced = findViewById(R.id.tv_step_placed);
        lineStep12 = findViewById(R.id.line_step_1_2);
        ivStepPreparing = findViewById(R.id.iv_step_preparing);
        tvStepPreparing = findViewById(R.id.tv_step_preparing);
        lineStep23 = findViewById(R.id.line_step_2_3);
        ivStepOut = findViewById(R.id.iv_step_out);
        tvStepOut = findViewById(R.id.tv_step_out);
        lineStep34 = findViewById(R.id.line_step_3_4);
        ivStepDelivered = findViewById(R.id.iv_step_delivered);
        tvStepDelivered = findViewById(R.id.tv_step_delivered);
        layoutCancelledBanner = findViewById(R.id.layout_cancelled_banner);

        tvAddress = findViewById(R.id.tv_detail_address);
        tvPhone = findViewById(R.id.tv_detail_phone);
        layoutPhone = findViewById(R.id.layout_detail_phone);
        tvPaymentInfo = findViewById(R.id.tv_detail_payment_info);
        tvNote = findViewById(R.id.tv_detail_note);

        layoutItemsContainer = findViewById(R.id.layout_items_container);

        tvSubtotal = findViewById(R.id.tv_detail_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_detail_delivery_fee);
        tvTax = findViewById(R.id.tv_detail_tax);
        layoutDiscount = findViewById(R.id.layout_detail_discount);
        tvDiscount = findViewById(R.id.tv_detail_discount);
        tvGrandTotal = findViewById(R.id.tv_detail_grand_total);

        btnLeaveReview = findViewById(R.id.btn_leave_review);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);

        btnCancelOrder.setOnClickListener(v -> confirmCancelOrder());
        btnLeaveReview.setOnClickListener(v -> {
            if (currentOrder != null && currentOrder.getItems() != null && !currentOrder.getItems().isEmpty()) {
                showReviewDialog(currentOrder.getItems().get(0));
            }
        });
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadOrderDetails() {
        AppExecutor.getInstance().execute(
                () -> orderDao.getOrderById(orderId),
                order -> {
                    if (order == null) {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    currentOrder = order;
                    displayOrder(order);
                }
        );
    }

    private void displayOrder(Order order) {
        String formattedId = String.format(Locale.US, "#TGO-%04d", order.getId());
        toolbar.setTitle(formattedId);
        tvOrderId.setText(formattedId);
        tvDateTime.setText("Placed on " + order.getCreatedAt());

        // Status badge
        String status = order.getStatus();
        tvStatusBadge.setText(status.replace("_", " "));

        int badgeBg;
        int badgeText;
        if (Order.STATUS_DELIVERED.equalsIgnoreCase(status)) {
            badgeBg = R.color.status_delivered_bg;
            badgeText = R.color.status_delivered_text;
        } else if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            badgeBg = R.color.status_cancelled_bg;
            badgeText = R.color.status_cancelled_text;
        } else if (Order.STATUS_PREPARING.equalsIgnoreCase(status)) {
            badgeBg = R.color.status_preparing_bg;
            badgeText = R.color.status_preparing_text;
        } else if (Order.STATUS_OUT_FOR_DELIVERY.equalsIgnoreCase(status)) {
            badgeBg = R.color.status_out_delivery_bg;
            badgeText = R.color.status_out_delivery_text;
        } else {
            badgeBg = R.color.status_placed_bg;
            badgeText = R.color.status_placed_text;
        }

        tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, badgeBg)));
        tvStatusBadge.setTextColor(ContextCompat.getColor(this, badgeText));

        // Stepper
        updateStepper(status);

        // Address & Phone
        if (Order.TYPE_PICKUP.equalsIgnoreCase(order.getOrderType())) {
            tvAddress.setText("Store Pickup: TastyGo Kitchen, 100 Gourmet Way");
        } else {
            tvAddress.setText(order.getAddressText() != null && !order.getAddressText().isEmpty()
                    ? order.getAddressText() : "Default Delivery Address");
        }

        String phone = order.getUserPhone();
        if (phone != null && !phone.isEmpty()) {
            tvPhone.setText(phone);
            layoutPhone.setVisibility(View.VISIBLE);
        } else {
            layoutPhone.setVisibility(View.GONE);
        }

        tvPaymentInfo.setText(order.getPaymentMethod() + " • " + order.getOrderType());

        String note = order.getNote();
        if (note != null && !note.trim().isEmpty()) {
            tvNote.setText("Special Instructions: " + note);
            tvNote.setVisibility(View.VISIBLE);
        } else {
            tvNote.setVisibility(View.GONE);
        }

        // Populate items
        populateItems(order.getItems(), Order.STATUS_DELIVERED.equalsIgnoreCase(status));

        // Bill Breakdown
        tvSubtotal.setText(String.format(Locale.US, "$%.2f", order.getSubtotal()));
        tvDeliveryFee.setText(String.format(Locale.US, "$%.2f", order.getDeliveryFee()));
        tvTax.setText(String.format(Locale.US, "$%.2f", order.getTax()));

        if (order.getDiscount() > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText(String.format(Locale.US, "-$%.2f", order.getDiscount()));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }

        tvGrandTotal.setText(String.format(Locale.US, "$%.2f", order.getTotal()));

        // Actions visibility
        if (order.canBeCancelled()) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }

        if (Order.STATUS_DELIVERED.equalsIgnoreCase(status)) {
            btnLeaveReview.setVisibility(View.VISIBLE);
        } else {
            btnLeaveReview.setVisibility(View.GONE);
        }
    }

    private void updateStepper(String status) {
        int colorChili = ContextCompat.getColor(this, R.color.primary_chili);
        int colorBasil = ContextCompat.getColor(this, R.color.success_basil);
        int colorMist = ContextCompat.getColor(this, R.color.mist);
        int colorCaption = ContextCompat.getColor(this, R.color.text_caption);
        int colorWhite = ContextCompat.getColor(this, R.color.text_white);
        int colorBorder = ContextCompat.getColor(this, R.color.border_line);

        if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            layoutStepper.setVisibility(View.GONE);
            layoutCancelledBanner.setVisibility(View.VISIBLE);
            tvTrackerSubtitle.setText("This order was cancelled and will not be delivered.");
            return;
        }

        layoutStepper.setVisibility(View.VISIBLE);
        layoutCancelledBanner.setVisibility(View.GONE);

        // Reset all to default inactive
        setStepState(ivStepPlaced, tvStepPlaced, colorMist, colorCaption, false);
        setStepState(ivStepPreparing, tvStepPreparing, colorMist, colorCaption, false);
        setStepState(ivStepOut, tvStepOut, colorMist, colorCaption, false);
        setStepState(ivStepDelivered, tvStepDelivered, colorMist, colorCaption, false);
        lineStep12.setBackgroundColor(colorBorder);
        lineStep23.setBackgroundColor(colorBorder);
        lineStep34.setBackgroundColor(colorBorder);

        if (Order.STATUS_PLACED.equalsIgnoreCase(status) || Order.STATUS_CONFIRMED.equalsIgnoreCase(status)) {
            setStepState(ivStepPlaced, tvStepPlaced, colorChili, colorWhite, true);
            tvStepPlaced.setTextColor(colorChili);
            tvTrackerSubtitle.setText("Order placed! The kitchen will begin preparation shortly.");
        } else if (Order.STATUS_PREPARING.equalsIgnoreCase(status)) {
            setStepState(ivStepPlaced, tvStepPlaced, colorChili, colorWhite, false);
            lineStep12.setBackgroundColor(colorChili);
            setStepState(ivStepPreparing, tvStepPreparing, colorChili, colorWhite, true);
            tvStepPreparing.setTextColor(colorChili);
            tvTrackerSubtitle.setText("Chef is cooking your meal with fresh ingredients.");
        } else if (Order.STATUS_OUT_FOR_DELIVERY.equalsIgnoreCase(status)) {
            setStepState(ivStepPlaced, tvStepPlaced, colorChili, colorWhite, false);
            lineStep12.setBackgroundColor(colorChili);
            setStepState(ivStepPreparing, tvStepPreparing, colorChili, colorWhite, false);
            lineStep23.setBackgroundColor(colorChili);
            setStepState(ivStepOut, tvStepOut, colorChili, colorWhite, true);
            tvStepOut.setTextColor(colorChili);
            tvTrackerSubtitle.setText("Your order is on the way! Rider will arrive soon.");
        } else if (Order.STATUS_DELIVERED.equalsIgnoreCase(status)) {
            setStepState(ivStepPlaced, tvStepPlaced, colorBasil, colorWhite, false);
            lineStep12.setBackgroundColor(colorBasil);
            setStepState(ivStepPreparing, tvStepPreparing, colorBasil, colorWhite, false);
            lineStep23.setBackgroundColor(colorBasil);
            setStepState(ivStepOut, tvStepOut, colorBasil, colorWhite, false);
            lineStep34.setBackgroundColor(colorBasil);
            setStepState(ivStepDelivered, tvStepDelivered, colorBasil, colorWhite, true);
            tvStepDelivered.setTextColor(colorBasil);
            tvTrackerSubtitle.setText("Delivered! Enjoy your meal.");
        }
    }

    private void setStepState(ImageView icon, TextView label, int bgTint, int iconTint, boolean isActive) {
        icon.setBackgroundTintList(ColorStateList.valueOf(bgTint));
        icon.setImageTintList(ColorStateList.valueOf(iconTint));
        label.setTextColor(isActive ? bgTint : ContextCompat.getColor(this, R.color.text_caption));
    }

    private void populateItems(List<OrderItem> items, boolean isDelivered) {
        layoutItemsContainer.removeAllViews();
        if (items == null || items.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (OrderItem item : items) {
            View itemView = inflater.inflate(android.R.layout.simple_list_item_2, layoutItemsContainer, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            text1.setText(item.getQuantity() + "x  " + item.getNameSnapshot());
            text1.setTextColor(ContextCompat.getColor(this, R.color.text_ink));
            text1.setTextSize(13);
            text1.setTypeface(null, android.graphics.Typeface.BOLD);

            double lineTotal = item.getPriceSnapshot() * item.getQuantity();
            text2.setText(String.format(Locale.US, "$%.2f ($%.2f each)", lineTotal, item.getPriceSnapshot()));
            text2.setTextColor(ContextCompat.getColor(this, R.color.text_caption));
            text2.setTextSize(12);

            itemView.setPadding(0, 8, 0, 8);

            if (isDelivered && item.getFoodId() != null && item.getFoodId() > 0) {
                itemView.setOnClickListener(v -> showReviewDialog(item));
            }

            layoutItemsContainer.addView(itemView);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(ContextCompat.getColor(this, R.color.border_line));
            layoutItemsContainer.addView(divider);
        }
    }

    private void confirmCancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    AppExecutor.getInstance().execute(
                            () -> orderDao.cancelOrder(orderId, sessionManager.getUserId()),
                            success -> {
                                if (Boolean.TRUE.equals(success)) {
                                    Toast.makeText(OrderDetailActivity.this, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                                    loadOrderDetails();
                                } else {
                                    Toast.makeText(OrderDetailActivity.this, "Cannot cancel this order", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                })
                .setNegativeButton("Keep Order", null)
                .show();
    }

    private void showReviewDialog(OrderItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_food, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvFoodName = dialogView.findViewById(R.id.tv_dialog_food_name);
        TextView tvRatingDesc = dialogView.findViewById(R.id.tv_rating_description);
        EditText etComment = dialogView.findViewById(R.id.et_review_comment);
        View btnDismiss = dialogView.findViewById(R.id.btn_dismiss_review);
        View btnSubmit = dialogView.findViewById(R.id.btn_submit_review);

        ImageView[] stars = new ImageView[]{
                dialogView.findViewById(R.id.star_1),
                dialogView.findViewById(R.id.star_2),
                dialogView.findViewById(R.id.star_3),
                dialogView.findViewById(R.id.star_4),
                dialogView.findViewById(R.id.star_5)
        };

        tvFoodName.setText(item.getNameSnapshot());

        final int[] selectedRating = {5};
        updateStarDisplay(stars, tvRatingDesc, 5);

        for (int i = 0; i < stars.length; i++) {
            final int starIndex = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedRating[0] = starIndex;
                updateStarDisplay(stars, tvRatingDesc, starIndex);
            });
        }

        btnDismiss.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            long foodId = item.getFoodId() != null ? item.getFoodId() : 1;

            Review review = new Review();
            review.setUserId(sessionManager.getUserId());
            review.setFoodId(foodId);
            review.setRating(selectedRating[0]);
            review.setComment(comment.isEmpty() ? "Great taste!" : comment);
            review.setCreatedAt(DateUtil.now());

            AppExecutor.getInstance().execute(
                    () -> reviewDao.insertReview(review),
                    reviewId -> {
                        dialog.dismiss();
                        if (reviewId != null && reviewId > 0) {
                            Toast.makeText(OrderDetailActivity.this, "Thank you! Your review has been submitted.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OrderDetailActivity.this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        dialog.show();
    }

    private void updateStarDisplay(ImageView[] stars, TextView desc, int rating) {
        String[] descriptions = {
                "1.0 - Poor",
                "2.0 - Fair",
                "3.0 - Good",
                "4.0 - Very Good",
                "5.0 - Excellent!"
        };
        desc.setText(descriptions[rating - 1]);

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
                stars[i].setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.rating_gold)));
            } else {
                stars[i].setImageResource(R.drawable.ic_star_border);
                stars[i].setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.rating_gold)));
            }
        }
    }
}
