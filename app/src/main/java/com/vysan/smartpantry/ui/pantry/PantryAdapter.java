package com.vysan.smartpantry.ui.pantry;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vysan.smartpantry.R;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.logic.ExpiryHelper;
import com.vysan.smartpantry.ui.common.Format;

import java.time.LocalDate;

/**
 * Binds pantry items from the database to the cards in the pantry RecyclerView.
 * ListAdapter + DiffUtil work out which rows changed, so only those rows are redrawn and animated.
 */
public class PantryAdapter extends ListAdapter<PantryItem, PantryAdapter.ViewHolder> {

    /** Called when the user taps a card. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private static final DiffUtil.ItemCallback<PantryItem> DIFF = new DiffUtil.ItemCallback<PantryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PantryItem oldItem, @NonNull PantryItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    private final int soonWindow;
    private OnItemClickListener listener;

    public PantryAdapter(int soonWindow) {
        super(DIFF);
        this.soonWindow = soonWindow;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        ViewHolder holder = new ViewHolder(view);
        // One listener per recycled row, looking up whichever item the row shows when tapped.
        view.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(getItem(position));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = getItem(position);
        Context context = holder.itemView.getContext();
        holder.name.setText(item.getName());
        holder.quantity.setText(context.getString(R.string.quantity_with_unit,
                Format.quantity(item.getQuantity()), item.getUnit()));
        bindExpiryChip(holder.expiry, item.getExpiryDate());
    }

    /** Public so the swipe-to-delete handler can find which item was swiped. */
    public PantryItem getItemAt(int position) {
        return getItem(position);
    }

    // Green = fine, amber = within the expiring-soon window, red = expired, hidden = no date.
    private void bindExpiryChip(TextView chip, String expiryDate) {
        LocalDate today = LocalDate.now();
        ExpiryHelper.Status status = ExpiryHelper.statusOf(expiryDate, today, soonWindow);
        if (status == ExpiryHelper.Status.NONE) {
            chip.setVisibility(View.GONE);
            return;
        }
        Context context = chip.getContext();
        long days = ExpiryHelper.daysUntil(expiryDate, today);
        int background;
        int text;
        if (status == ExpiryHelper.Status.EXPIRED) {
            chip.setText(R.string.chip_expired);
            background = R.color.chip_expired_bg;
            text = R.color.chip_expired_text;
        } else if (status == ExpiryHelper.Status.SOON) {
            chip.setText(days == 0 ? context.getString(R.string.chip_today)
                    : context.getResources().getQuantityString(R.plurals.chip_days_left, (int) days, (int) days));
            background = R.color.chip_soon_bg;
            text = R.color.chip_soon_text;
        } else {
            chip.setText(context.getString(R.string.chip_use_by, Format.displayDate(expiryDate)));
            background = R.color.chip_fresh_bg;
            text = R.color.chip_fresh_text;
        }
        chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, background)));
        chip.setTextColor(ContextCompat.getColor(context, text));
        chip.setVisibility(View.VISIBLE);
    }

    /** Caches the row's views so findViewById runs once per row, not once per bind. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiry = itemView.findViewById(R.id.item_expiry);
        }
    }
}
