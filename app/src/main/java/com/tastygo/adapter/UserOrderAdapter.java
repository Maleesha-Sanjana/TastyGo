package com.tastygo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.tvTotal.setText(String.format(Locale.US, "$%.2f", order.getTotal()));

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
        TextView tvOrderDate;
        TextView tvItemsSummary;
        TextView tvTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tv_order_number);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvItemsSummary = itemView.findViewById(R.id.tv_order_items_summary);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
        }
    }
}
