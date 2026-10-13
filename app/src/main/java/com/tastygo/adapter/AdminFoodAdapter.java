package com.tastygo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tastygo.R;
import com.tastygo.databinding.ItemAdminFoodBinding;
import com.tastygo.model.FoodItem;
import com.tastygo.util.ImageUtil;

import java.util.Locale;

public class AdminFoodAdapter extends ListAdapter<FoodItem, AdminFoodAdapter.AdminFoodViewHolder> {

    public interface OnFoodActionListener {
        void onEdit(FoodItem item);
        void onDelete(FoodItem item);
        void onAvailabilityChanged(FoodItem item, boolean isAvailable);
    }

    private final OnFoodActionListener listener;

    public AdminFoodAdapter(OnFoodActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FoodItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<FoodItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull FoodItem oldItem, @NonNull FoodItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FoodItem oldItem, @NonNull FoodItem newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    oldItem.isAvailable() == newItem.isAvailable() &&
                    oldItem.isVeg() == newItem.isVeg() &&
                    oldItem.getAvgRating() == newItem.getAvgRating() &&
                    oldItem.getImagePath().equals(newItem.getImagePath());
        }
    };

    @NonNull
    @Override
    public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminFoodBinding binding = ItemAdminFoodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminFoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class AdminFoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminFoodBinding binding;

        public AdminFoodViewHolder(@NonNull ItemAdminFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bind(FoodItem item) {
            Context context = itemView.getContext();

            binding.tvAdminFoodName.setText(item.getName());
            binding.tvAdminFoodPrice.setText(String.format(Locale.US, "$%.2f", item.getPrice()));
            binding.tvAdminFoodRating.setText(String.format(Locale.US, "%.1f", item.getAvgRating()));

            String catName = item.getCategoryName();
            if (catName != null && !catName.isEmpty()) {
                binding.tvAdminFoodCategory.setText(catName);
                binding.tvAdminFoodCategory.setVisibility(ViewGroup.VISIBLE);
            } else {
                binding.tvAdminFoodCategory.setVisibility(ViewGroup.GONE);
            }

            // Veg vs Non-Veg
            binding.ivAdminVegIndicator.setImageResource(item.isVeg() ? R.drawable.ic_veg : R.drawable.ic_non_veg);

            // Load Food Thumbnail
            ImageUtil.loadImage(context, item.getImagePath(), binding.ivAdminFoodThumb, R.drawable.ic_food_placeholder);

            // Availability Switch
            binding.switchAvailability.setOnCheckedChangeListener(null);
            binding.switchAvailability.setChecked(item.isAvailable());
            binding.tvAvailabilityLabel.setText(item.isAvailable() ? "In Stock" : "Out of Stock");

            binding.switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setAvailable(isChecked);
                binding.tvAvailabilityLabel.setText(isChecked ? "In Stock" : "Out of Stock");
                if (listener != null) {
                    listener.onAvailabilityChanged(item, isChecked);
                }
            });

            // Action Buttons
            binding.btnAdminEditFood.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item);
            });

            binding.btnAdminDeleteFood.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(item);
            });
        }
    }
}
