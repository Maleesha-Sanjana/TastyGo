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

    private final Context context;
    private final List<Order> orders = new ArrayList<>();

    public UserOrderAdapter(Context context) {
        this.context = context;
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
        holder.tvTotal.setText(String.format(Locale.US, "Rs. %.2f", order.getTotal()));

        // Order state / status badge
        String status = order.getStatus();
        if (status == null || status.trim().isEmpty()) {
            status = Order.STATUS_PLACED;
        }
        holder.tvOrderStatus.setText(status.replace("_", " ").toUpperCase(Locale.US));

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

        holder.tvOrderStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, bgRes)));
        holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, textRes));

        // Items summary: displays what was ordered
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
            sb.append(order.getOrderType()).append(" • Payment: ").append(order.getPaymentMethod());
        }
        holder.tvItemsSummary.setText(sb.toString());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber;
        TextView tvOrderStatus;
        TextView tvOrderDate;
        TextView tvItemsSummary;
        TextView tvTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tv_order_number);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvItemsSummary = itemView.findViewById(R.id.tv_order_items_summary);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
        }
    }
}
