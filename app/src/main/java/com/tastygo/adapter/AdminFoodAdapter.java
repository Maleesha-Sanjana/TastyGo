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
                    oldItem.getStock() == newItem.getStock() &&
                    oldItem.isAvailable() == newItem.isAvailable() &&
                    oldItem.isPopular() == newItem.isPopular() &&
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
            binding.tvAdminFoodPrice.setText(String.format(Locale.US, "Rs. %.2f", item.getPrice()));

            binding.tvAdminFoodPopular.setVisibility(item.isPopular() ? android.view.View.VISIBLE : android.view.View.GONE);

            int stock = item.getStock();
            if (stock <= 0) {
                binding.tvAdminFoodStock.setText("Out of Stock (0)");
                binding.tvAdminFoodStock.setTextColor(context.getColor(R.color.non_veg_red));
                binding.tvAdminFoodStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.status_cancelled_bg)));
            } else if (stock <= 5) {
                binding.tvAdminFoodStock.setText("Low Stock: " + stock);
                binding.tvAdminFoodStock.setTextColor(context.getColor(R.color.text_ink));
                binding.tvAdminFoodStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.status_preparing_bg)));
            } else {
                binding.tvAdminFoodStock.setText("In Stock: " + stock);
                binding.tvAdminFoodStock.setTextColor(context.getColor(R.color.success_basil));
                binding.tvAdminFoodStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.status_delivered_bg)));
            }

            ImageUtil.loadImage(context, item.getImagePath(), binding.ivAdminFoodThumb, R.drawable.ic_food_placeholder);

            binding.switchAvailability.setOnCheckedChangeListener(null);
            binding.switchAvailability.setChecked(item.isAvailable() && stock > 0);
            binding.tvAvailabilityLabel.setText(item.isAvailable() && stock > 0 ? "Active" : "Inactive");

            binding.switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setAvailable(isChecked);
                binding.tvAvailabilityLabel.setText(isChecked ? "Active" : "Inactive");
                if (listener != null) {
                    listener.onAvailabilityChanged(item, isChecked);
                }
            });

            binding.btnAdminEditFood.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item);
            });

            binding.btnAdminDeleteFood.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(item);
            });
        }
    }
}
