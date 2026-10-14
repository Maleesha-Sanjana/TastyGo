package com.tastygo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.tastygo.R;
import com.tastygo.model.FoodItem;
import com.tastygo.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserFoodAdapter extends RecyclerView.Adapter<UserFoodAdapter.FoodViewHolder> {

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem item);
        void onAddClick(FoodItem item);
    }

    private final Context context;
    private final List<FoodItem> items = new ArrayList<>();
    private final OnFoodClickListener listener;

    public UserFoodAdapter(Context context, OnFoodClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<FoodItem> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newItems != null ? newItems.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return items.get(oldItemPosition).getId() == newItems.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                FoodItem oldItem = items.get(oldItemPosition);
                FoodItem newItem = newItems.get(newItemPosition);
                return oldItem.isAvailable() == newItem.isAvailable()
                        && Double.compare(oldItem.getPrice(), newItem.getPrice()) == 0
                        && oldItem.getName().equals(newItem.getName());
            }
        });

        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = items.get(position);

        holder.tvTitle.setText(item.getName());
        holder.tvDesc.setText(item.getDescription());
        holder.tvPrice.setText(String.format(Locale.US, "Rs. %.2f", item.getPrice()));

        String category = item.getCategoryName();
        if (category == null || category.isEmpty()) {
            category = "Tasty Special";
        }
        holder.tvCategory.setText(category);

        // Veg or Non-Veg badge
        if (item.isVeg()) {
            holder.ivVegBadge.setImageResource(R.drawable.ic_veg);
        } else {
            holder.ivVegBadge.setImageResource(R.drawable.ic_non_veg);
        }

        // Load food image
        ImageUtil.loadImage(context, item.getImagePath(), holder.ivThumb, R.drawable.ic_food_placeholder);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFoodClick(item);
        });

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAddClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        ImageView ivVegBadge;
        TextView tvCategory;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvPrice;
        MaterialButton btnAdd;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_food_thumb);
            ivVegBadge = itemView.findViewById(R.id.iv_food_veg_badge);
            tvCategory = itemView.findViewById(R.id.tv_food_category);
            tvTitle = itemView.findViewById(R.id.tv_food_title);
            tvDesc = itemView.findViewById(R.id.tv_food_desc);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            btnAdd = itemView.findViewById(R.id.btn_quick_add);
        }
    }
}
