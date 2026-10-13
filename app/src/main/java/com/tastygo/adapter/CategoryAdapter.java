package com.tastygo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tastygo.R;
import com.tastygo.databinding.ItemAdminCategoryBinding;
import com.tastygo.model.Category;

import java.util.HashMap;
import java.util.Map;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private final OnCategoryActionListener listener;
    private Map<Long, Integer> itemCounts = new HashMap<>();

    public CategoryAdapter(OnCategoryActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setItemCounts(Map<Long, Integer> itemCounts) {
        this.itemCounts = itemCounts != null ? itemCounts : new HashMap<>();
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = new DiffUtil.ItemCallback<Category>() {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getImagePath().equals(newItem.getImagePath());
        }
    };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminCategoryBinding binding = ItemAdminCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminCategoryBinding binding;

        public CategoryViewHolder(@NonNull ItemAdminCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Category category) {
            binding.tvCategoryName.setText(category.getName());

            int count = itemCounts.containsKey(category.getId()) ? itemCounts.get(category.getId()) : 0;
            binding.tvCategoryCount.setText(count + (count == 1 ? " menu item" : " menu items"));

            Context context = itemView.getContext();
            int resId = 0;
            if (category.getImagePath() != null && !category.getImagePath().isEmpty()) {
                resId = context.getResources().getIdentifier(category.getImagePath(), "drawable", context.getPackageName());
            }
            if (resId != 0) {
                binding.ivCategoryIcon.setImageResource(resId);
            } else {
                binding.ivCategoryIcon.setImageResource(R.drawable.ic_category);
            }

            binding.btnEditCategory.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(category);
            });

            binding.btnDeleteCategory.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(category);
            });
        }
    }
}
