package com.vysan.smartpantry.ui.recipes;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.vysan.smartpantry.R;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.model.RecipeIngredient;
import com.vysan.smartpantry.data.repo.PantryRepository;
import com.vysan.smartpantry.logic.RecipeMatcher;
import com.vysan.smartpantry.ui.common.ClickGuard;
import com.vysan.smartpantry.ui.common.Format;

import java.util.List;

/**
 * Full recipe: time and servings, the ingredient list with a tick or cross per ingredient
 * (checked live against the pantry), and the numbered method. Opened with EXTRA_RECIPE_ID.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.vysan.smartpantry.EXTRA_RECIPE_ID";
    private static final long NO_RECIPE = -1L;

    private final RecipeMatcher matcher = new RecipeMatcher();
    private Recipe recipe;
    private List<PantryItem> pantry;

    /** Builds the explicit Intent that opens this screen for one recipe. */
    public static void start(Context context, long recipeId) {
        if (!ClickGuard.allow()) return;
        context.startActivity(new Intent(context, RecipeDetailActivity.class).putExtra(EXTRA_RECIPE_ID, recipeId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, NO_RECIPE);
        PantryRepository repository = PantryRepository.getInstance(this);
        repository.loadRecipe(recipeId, loaded -> {
            if (loaded == null) {
                Toast.makeText(this, R.string.recipe_missing, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            recipe = loaded;
            render();
        });
        // Observing the pantry keeps the ticks correct if it changes while this screen is open.
        repository.getPantryItems().observe(this, items -> {
            pantry = items;
            render();
        });
    }

    private void render() {
        if (recipe == null || pantry == null) return;   // wait until both have loaded
        CollapsingToolbarLayout collapsing = findViewById(R.id.collapsing_toolbar);
        collapsing.setTitle(recipe.getName());
        ((TextView) findViewById(R.id.recipe_description)).setText(recipe.getDescription());
        ((Chip) findViewById(R.id.chip_time)).setText(getString(R.string.chip_minutes, recipe.getPrepMinutes()));
        ((Chip) findViewById(R.id.chip_servings)).setText(
                getResources().getQuantityString(R.plurals.chip_servings, recipe.getServings(), recipe.getServings()));

        List<RecipeIngredient> missing = matcher.findShortfalls(pantry, recipe);
        renderStatus(missing);
        renderIngredients(missing);
        renderSteps();
    }

    private void renderStatus(List<RecipeIngredient> missing) {
        TextView status = findViewById(R.id.recipe_status);
        boolean ready = missing.isEmpty();
        if (ready) {
            status.setText(R.string.detail_ready);
        } else if (missing.size() == 1) {
            status.setText(getString(R.string.tag_missing, missing.get(0).getName()));
        } else {
            status.setText(getResources().getQuantityString(R.plurals.detail_missing_count, missing.size(), missing.size()));
        }
        status.setTextColor(ContextCompat.getColor(this, ready ? R.color.chip_fresh_text : R.color.tomato_text));
        status.setCompoundDrawablesRelativeWithIntrinsicBounds(ready ? R.drawable.ic_check_circle : R.drawable.ic_warning, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(status, ColorStateList.valueOf(status.getCurrentTextColor()));
    }

    // One row per ingredient: green tick when the pantry covers it, tomato cross when it does not.
    private void renderIngredients(List<RecipeIngredient> missing) {
        LinearLayout container = findViewById(R.id.ingredients_container);
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            TextView row = (TextView) inflater.inflate(R.layout.item_detail_ingredient, container, false);
            boolean have = !missing.contains(ingredient);
            String line = getString(R.string.detail_ingredient,
                    Format.quantity(ingredient.getQuantity()), ingredient.getUnit(), ingredient.getName());
            row.setText(line);
            row.setCompoundDrawablesRelativeWithIntrinsicBounds(have ? R.drawable.ic_check_circle : R.drawable.ic_cancel, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(row, ColorStateList.valueOf(
                    ContextCompat.getColor(this, have ? R.color.primary : R.color.tomato_text)));
            // The icon carries meaning, so screen readers hear it as part of the line.
            row.setContentDescription(getString(have ? R.string.detail_have : R.string.detail_missing) + ": " + line);
            container.addView(row);
        }
    }

    private void renderSteps() {
        LinearLayout container = findViewById(R.id.steps_container);
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        List<String> steps = recipe.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            View row = inflater.inflate(R.layout.item_detail_step, container, false);
            ((TextView) row.findViewById(R.id.step_number)).setText(String.valueOf(i + 1));
            ((TextView) row.findViewById(R.id.step_text)).setText(steps.get(i));
            container.addView(row);
        }
    }
}
