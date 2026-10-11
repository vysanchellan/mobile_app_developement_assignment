package com.vysan.smartpantry.ui.recipes;

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
import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.model.RecipeIngredient;
import com.vysan.smartpantry.ui.common.Format;

import java.util.Objects;

/**
 * Shows recipe cards for both tabs. A ready row carries no missing ingredient; an almost-there row
 * names the one ingredient that is missing or short.
 */
public class RecipeAdapter extends ListAdapter<RecipeAdapter.Row, RecipeAdapter.ViewHolder> {

    /** One card: a recipe plus, for almost-there rows, the ingredient holding it back. */
    public static final class Row {
        final Recipe recipe;
        final RecipeIngredient missing;
        final boolean partlyStocked;

        private Row(Recipe recipe, RecipeIngredient missing, boolean partlyStocked) {
            this.recipe = recipe;
            this.missing = missing;
            this.partlyStocked = partlyStocked;
        }

        public static Row ready(Recipe recipe) {
            return new Row(recipe, null, false);
        }

        /** @param partlyStocked true when the pantry has some of the ingredient, just not enough */
        public static Row almost(Recipe recipe, RecipeIngredient missing, boolean partlyStocked) {
            return new Row(recipe, missing, partlyStocked);
        }

        private String missingName() {
            return missing == null ? null : missing.getName();
        }
    }

    /** Called when the user taps a recipe card. */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private static final DiffUtil.ItemCallback<Row> DIFF = new DiffUtil.ItemCallback<Row>() {
        @Override
        public boolean areItemsTheSame(@NonNull Row oldRow, @NonNull Row newRow) {
            return oldRow.recipe.getId() == newRow.recipe.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Row oldRow, @NonNull Row newRow) {
            return Objects.equals(oldRow.missingName(), newRow.missingName())
                    && oldRow.partlyStocked == newRow.partlyStocked;
        }
    };

    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) listener.onRecipeClick(getItem(position).recipe);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Row row = getItem(position);
        Context context = holder.itemView.getContext();
        Recipe recipe = row.recipe;
        holder.name.setText(recipe.getName());
        int count = recipe.getIngredients().size();
        holder.meta.setText(context.getString(R.string.recipe_meta, recipe.getPrepMinutes(), recipe.getServings(),
                context.getResources().getQuantityString(R.plurals.ingredient_count, count, count)));
        if (row.missing == null) {
            holder.tag.setText(R.string.tag_have_everything);
            holder.tag.setTextColor(ContextCompat.getColor(context, R.color.chip_fresh_text));
            holder.tag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chip_fresh_bg)));
        } else {
            holder.tag.setText(missingLabel(context, row));
            holder.tag.setTextColor(ContextCompat.getColor(context, R.color.missing_tag_text));
            holder.tag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.missing_tag_bg)));
        }
    }

    // "Missing: Butter" when absent, "Need more: Eggs (3 pcs)" when the pantry has too little.
    static String missingLabel(Context context, Row row) {
        RecipeIngredient missing = row.missing;
        if (!row.partlyStocked) return context.getString(R.string.tag_missing, missing.getName());
        return context.getString(R.string.tag_need_more, missing.getName(),
                Format.quantity(missing.getQuantity()), missing.getUnit());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView tag;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            meta = itemView.findViewById(R.id.recipe_meta);
            tag = itemView.findViewById(R.id.recipe_tag);
        }
    }
}
