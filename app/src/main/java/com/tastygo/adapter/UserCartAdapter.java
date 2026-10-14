package com.tastygo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tastygo.R;
import com.tastygo.model.CartItem;
import com.tastygo.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserCartAdapter extends RecyclerView.Adapter<UserCartAdapter.CartViewHolder> {

    public interface OnCartItemActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemDeleted(CartItem item);
    }

    private final Context context;
    private final List<CartItem> items = new ArrayList<>();
    private final OnCartItemActionListener listener;

    public UserCartAdapter(Context context, OnCartItemActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<CartItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);

        holder.tvName.setText(item.getFoodName());
        holder.tvPrice.setText(String.format(Locale.US, "Rs. %.2f", item.getItemTotal()));
        holder.tvQty.setText(String.valueOf(item.getQuantity()));

        StringBuilder optionsText = new StringBuilder();
        if (item.getSize() != null && !item.getSize().trim().isEmpty()) {
            optionsText.append(item.getSize().trim());
        }
        if (item.getAddons() != null && !item.getAddons().trim().isEmpty()) {
            if (optionsText.length() > 0) optionsText.append(" • ");
            optionsText.append(item.getAddons().trim());
        }

        if (optionsText.length() > 0) {
            holder.tvOptions.setVisibility(View.VISIBLE);
            holder.tvOptions.setText(optionsText.toString());
        } else {
            holder.tvOptions.setVisibility(View.GONE);
        }

        ImageUtil.loadImage(context, item.getFoodImagePath(), holder.ivThumb, R.drawable.ic_food_placeholder);

        holder.btnMinus.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            if (listener != null) {
                listener.onQuantityChanged(item, newQty);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            if (listener != null) {
                listener.onQuantityChanged(item, newQty);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemDeleted(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName;
        TextView tvOptions;
        TextView tvPrice;
        TextView tvQty;
        ImageButton btnMinus;
        ImageButton btnPlus;
        ImageButton btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_cart_food_thumb);
            tvName = itemView.findViewById(R.id.tv_cart_food_name);
            tvOptions = itemView.findViewById(R.id.tv_cart_item_options);
            tvPrice = itemView.findViewById(R.id.tv_cart_item_price);
            tvQty = itemView.findViewById(R.id.tv_cart_qty);
            btnMinus = itemView.findViewById(R.id.btn_cart_minus);
            btnPlus = itemView.findViewById(R.id.btn_cart_plus);
            btnDelete = itemView.findViewById(R.id.btn_cart_delete);
        }
    }
}
