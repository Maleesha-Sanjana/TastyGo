package com.tastygo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.model.Order;
import com.tastygo.model.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {

    public interface OnAdminOrderActionListener {
        void onUpdateStatus(Order order);
        void onOrderClick(Order order);
        void onCallCustomer(String phone);
    }

    private final Context context;
    private final List<Order> orders = new ArrayList<>();
    private final OnAdminOrderActionListener listener;

    public AdminOrderAdapter(Context context, OnAdminOrderActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.tvOrderId.setText(String.format(Locale.US, "#TGO-%04d", order.getId()));
        holder.tvOrderType.setText(order.getOrderType().toUpperCase(Locale.US));

        // Customer details
        String customerName = order.getUserName();
        holder.tvCustomerName.setText(customerName != null && !customerName.isEmpty() ? customerName : "Guest User");
        holder.tvOrderTime.setText(order.getCreatedAt());

        String phone = order.getUserPhone();
        if (phone != null && !phone.isEmpty()) {
            holder.tvCustomerPhone.setText(phone);
            holder.layoutPhone.setVisibility(View.VISIBLE);
            holder.layoutPhone.setOnClickListener(v -> {
                if (listener != null) listener.onCallCustomer(phone);
            });
        } else {
            holder.layoutPhone.setVisibility(View.GONE);
        }

        String address = order.getAddressText();
        if (address != null && !address.isEmpty()) {
            holder.tvCustomerAddress.setText(address);
            holder.layoutAddress.setVisibility(View.VISIBLE);
        } else {
            if (Order.TYPE_PICKUP.equalsIgnoreCase(order.getOrderType())) {
                holder.tvCustomerAddress.setText("Store Pickup Order");
                holder.layoutAddress.setVisibility(View.VISIBLE);
            } else {
                holder.layoutAddress.setVisibility(View.GONE);
            }
        }

        // Status badge colors
        String status = order.getStatus();
        holder.tvStatus.setText(status.replace("_", " "));

        int bgRes;
        int textRes;
        if (Order.STATUS_DELIVERED.equalsIgnoreCase(status)) {
            bgRes = R.color.status_delivered_bg;
            textRes = R.color.status_delivered_text;
        } else if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            bgRes = R.color.status_cancelled_bg;
            textRes = R.color.status_cancelled_text;
        } else if (Order.STATUS_PREPARING.equalsIgnoreCase(status)) {
            bgRes = R.color.status_preparing_bg;
            textRes = R.color.status_preparing_text;
        } else if (Order.STATUS_OUT_FOR_DELIVERY.equalsIgnoreCase(status)) {
            bgRes = R.color.status_out_delivery_bg;
            textRes = R.color.status_out_delivery_text;
        } else {
            bgRes = R.color.status_placed_bg;
            textRes = R.color.status_placed_text;
        }

        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, bgRes)));
        holder.tvStatus.setTextColor(ContextCompat.getColor(context, textRes));

        // Items summary
        StringBuilder sb = new StringBuilder();
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            sb.append(items.size()).append(" item").append(items.size() > 1 ? "s: " : ": ");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) sb.append(", ");
                OrderItem oi = items.get(i);
                sb.append(oi.getQuantity()).append("x ").append(oi.getNameSnapshot());
            }
        } else {
            sb.append("Order details available in view");
        }
        holder.tvItemsSummary.setText(sb.toString());

        // Note
        String note = order.getNote();
        if (note != null && !note.trim().isEmpty()) {
            holder.tvOrderNote.setText("Note: " + note);
            holder.layoutNote.setVisibility(View.VISIBLE);
        } else {
            holder.layoutNote.setVisibility(View.GONE);
        }

        // Price & Payment
        holder.tvTotal.setText(String.format(Locale.US, "$%.2f", order.getTotal()));
        holder.tvPaymentMethod.setText("Payment: " + order.getPaymentMethod());

        // Buttons
        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (listener != null) listener.onUpdateStatus(order);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId;
        TextView tvOrderType;
        TextView tvStatus;
        TextView tvCustomerName;
        TextView tvOrderTime;
        TextView tvCustomerPhone;
        TextView tvCustomerAddress;
        TextView tvItemsSummary;
        TextView tvOrderNote;
        TextView tvTotal;
        TextView tvPaymentMethod;
        LinearLayout layoutPhone;
        LinearLayout layoutAddress;
        LinearLayout layoutNote;
        MaterialButton btnUpdateStatus;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_admin_order_id);
            tvOrderType = itemView.findViewById(R.id.tv_admin_order_type);
            tvStatus = itemView.findViewById(R.id.tv_admin_order_status);
            tvCustomerName = itemView.findViewById(R.id.tv_admin_customer_name);
            tvOrderTime = itemView.findViewById(R.id.tv_admin_order_time);
            tvCustomerPhone = itemView.findViewById(R.id.tv_admin_customer_phone);
            tvCustomerAddress = itemView.findViewById(R.id.tv_admin_customer_address);
            tvItemsSummary = itemView.findViewById(R.id.tv_admin_items_summary);
            tvOrderNote = itemView.findViewById(R.id.tv_admin_order_note);
            tvTotal = itemView.findViewById(R.id.tv_admin_order_total);
            tvPaymentMethod = itemView.findViewById(R.id.tv_admin_payment_method);
            layoutPhone = itemView.findViewById(R.id.layout_admin_customer_phone);
            layoutAddress = itemView.findViewById(R.id.layout_admin_customer_address);
            layoutNote = itemView.findViewById(R.id.layout_admin_order_note);
            btnUpdateStatus = itemView.findViewById(R.id.btn_admin_update_status);
        }
    }
}
