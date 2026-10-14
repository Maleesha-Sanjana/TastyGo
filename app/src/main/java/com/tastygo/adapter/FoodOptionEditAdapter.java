package com.tastygo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tastygo.databinding.ItemFoodOptionEditBinding;
import com.tastygo.model.FoodOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodOptionEditAdapter extends RecyclerView.Adapter<FoodOptionEditAdapter.OptionViewHolder> {

    public interface OnOptionDeleteListener {
        void onDelete(FoodOption option, int position);
    }

    private final List<FoodOption> options = new ArrayList<>();
    private final OnOptionDeleteListener listener;

    public FoodOptionEditAdapter(OnOptionDeleteListener listener) {
        this.listener = listener;
    }

    public void setOptions(List<FoodOption> newOptions) {
        options.clear();
        if (newOptions != null) {
            options.addAll(newOptions);
        }
        notifyDataSetChanged();
    }

    public void addOption(FoodOption option) {
        options.add(option);
        notifyItemInserted(options.size() - 1);
    }

    public void removeOptionAt(int position) {
        if (position >= 0 && position < options.size()) {
            options.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, options.size() - position);
        }
    }

    public List<FoodOption> getOptions() {
        return new ArrayList<>(options);
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodOptionEditBinding binding = ItemFoodOptionEditBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OptionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        holder.bind(options.get(position), position);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {
        private final ItemFoodOptionEditBinding binding;

        public OptionViewHolder(@NonNull ItemFoodOptionEditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FoodOption option, int position) {
            binding.tvOptionName.setText(option.getName());
            if (option.getExtraPrice() > 0) {
                binding.tvOptionExtraPrice.setText(String.format(Locale.US, "+Rs. %.2f", option.getExtraPrice()));
            } else {
                binding.tvOptionExtraPrice.setText("Free");
            }

            binding.btnDeleteOption.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDelete(option, pos);
                }
            });
        }
    }
}
