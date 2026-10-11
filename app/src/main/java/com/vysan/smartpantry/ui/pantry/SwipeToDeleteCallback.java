package com.vysan.smartpantry.ui.pantry;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.vysan.smartpantry.R;

/**
 * Swipe a pantry card to the left to delete it. Draws a red rounded background with a bin icon
 * behind the card while it is being dragged, so the action is obvious before it happens.
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    /** Receives the adapter position of the card that was swiped away. */
    public interface OnSwipedListener {
        void onSwiped(int position);
    }

    private final OnSwipedListener listener;
    private final GradientDrawable background = new GradientDrawable();
    private final Drawable icon;
    private final int iconMargin;

    public SwipeToDeleteCallback(Context context, OnSwipedListener listener) {
        super(0, ItemTouchHelper.LEFT);
        this.listener = listener;
        background.setCornerRadius(context.getResources().getDimension(R.dimen.corner_card));
        background.setColor(MaterialColors.getColor(context, androidx.appcompat.R.attr.colorError, 0));
        icon = ContextCompat.getDrawable(context, R.drawable.ic_delete).mutate();
        icon.setTintList(ColorStateList.valueOf(
                MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnError, 0)));
        iconMargin = context.getResources().getDimensionPixelSize(R.dimen.space_l);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;   // reordering is not supported; the list is sorted by expiry
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) listener.onSwiped(position);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        View card = viewHolder.itemView;
        if (dX < 0) {
            background.setBounds(card.getLeft(), card.getTop(), card.getRight(), card.getBottom());
            background.draw(c);
            int iconTop = card.getTop() + (card.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconRight = card.getRight() - iconMargin;
            icon.setBounds(iconRight - icon.getIntrinsicWidth(), iconTop, iconRight, iconTop + icon.getIntrinsicHeight());
            icon.draw(c);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
