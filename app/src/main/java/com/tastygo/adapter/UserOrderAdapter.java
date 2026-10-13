package com.tastygo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tastygo.R;
import com.tastygo.model.Order;
import com.tastygo.model.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserOrderAdapter extends RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final Context context;
    private final List<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public UserOrderAdapter(Context context, OnOrderClickListener listener) {
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
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.tvOrderNumber.setText(String.format(Locale.US, "#TGO-%04d", order.getId()));
        holder.tvOrderDate.setText(order.getCreatedAt());
        holder.tvTotal.setText(String.format(Locale.US, "$%.2f", order.getTotal()));

        // Status badge
        String status = order.getStatus();
        holder.tvStatus.setText(status.replace("_", " "));

        int badgeBg;
        int badgeText;
        if (Order.STATUS_DELIVERED.equalsIgnoreCase(status)) {
            badgeBg = R.color.basil_tint;
            badgeText = R.color.status_basil;
        } else if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            badgeBg = R.color.chili_tint;
            badgeText = R.color.primary_chili;
        } else if (Order.STATUS_PREPARING.equalsIgnoreCase(status) || Order.STATUS_OUT_FOR_DELIVERY.equalsIgnoreCase(status)) {
            badgeBg = R.color.chili_tint;
            badgeText = R.color.primary_chili;
        } else {
            badgeBg = R.color.mist;
            badgeText = R.color.text_ink;
        }

        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, badgeBg)));
        holder.tvStatus.setTextColor(ContextCompat.getColor(context, badgeText));

        // Items summary
        StringBuilder sb = new StringBuilder();
        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            sb.append(items.size()).append(" item").append(items.size() > 1 ? "s • " : " • ");
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(items.get(i).getFoodName());
            }
        } else {
            sb.append(order.getOrderType()).append(" • Payment: ").append(order.getPaymentMethod());
        }
        holder.tvItemsSummary.setText(sb.toString());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber;
        TextView tvOrderDate;
        TextView tvStatus;
        TextView tvItemsSummary;
        TextView tvTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tv_order_number);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvItemsSummary = itemView.findViewById(R.id.tv_order_items_summary);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
        }
    }
}
