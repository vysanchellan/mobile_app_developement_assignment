package com.vysan.smartpantry.logic;

import com.vysan.smartpantry.data.model.AlmostThere;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decides which recipes the user can cook. The strict rule: a recipe is suggested only when EVERY
 * required ingredient is in the pantry in at least the required quantity. Pure Java, no Android imports.
 */
public final class RecipeMatcher {

    /** Tolerance for floating point rounding, e.g. 0.1 kg converted to 100.00000000000001 g. */
    static final double EPSILON = 1e-6;

    /** Returns only recipes whose every ingredient is fully covered by the pantry. */
    public List<Recipe> findStrictMatches(List<PantryItem> pantry, List<Recipe> recipes) {
        Map<String, Map<UnitConverter.Family, Double>> stock = buildStock(pantry);
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (!canCook(recipe, stock)) continue;   // one missing or short ingredient rules it out
            matches.add(recipe);
        }
        return matches;
    }

    /** Returns recipes that are short of exactly one ingredient. Never overlaps with the strict list. */
    public List<AlmostThere> findAlmostThere(List<PantryItem> pantry, List<Recipe> recipes) {
        Map<String, Map<UnitConverter.Family, Double>> stock = buildStock(pantry);
        List<AlmostThere> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<RecipeIngredient> missing = shortfalls(recipe, stock);
            if (missing.size() == 1) result.add(new AlmostThere(recipe, missing.get(0)));
        }
        return result;
    }

    /** Lists the ingredients of one recipe that the pantry does not cover (used by the detail screen). */
    public List<RecipeIngredient> findShortfalls(List<PantryItem> pantry, Recipe recipe) {
        return shortfalls(recipe, buildStock(pantry));
    }

    // Early return on the first unmet ingredient: 4 out of 5 is still a "no".
    private boolean canCook(Recipe recipe, Map<String, Map<UnitConverter.Family, Double>> stock) {
        if (recipe.getIngredients().isEmpty()) return false;
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(needed, stock)) return false;
        }
        return true;
    }

    private List<RecipeIngredient> shortfalls(Recipe recipe,
                                              Map<String, Map<UnitConverter.Family, Double>> stock) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(needed, stock)) missing.add(needed);
        }
        return missing;
    }

    // Normalised name -> unit family -> total in base units. Duplicate rows are summed here.
    private Map<String, Map<UnitConverter.Family, Double>> buildStock(List<PantryItem> pantry) {
        Map<String, Map<UnitConverter.Family, Double>> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            UnitConverter.Family family = UnitConverter.familyOf(item.getUnit());
            if (family == null) continue;   // an unknown unit cannot be compared, so it cannot help
            String name = IngredientNormalizer.normalize(item.getName());
            double amount = UnitConverter.toBase(item.getQuantity(), item.getUnit());
            stock.computeIfAbsent(name, k -> new EnumMap<>(UnitConverter.Family.class))
                    .merge(family, amount, Double::sum);
        }
        return stock;
    }

    // Only stock in the same unit family counts: 2 pcs of cheese never satisfies 200 g of cheese.
    private boolean hasEnough(RecipeIngredient needed, Map<String, Map<UnitConverter.Family, Double>> stock) {
        UnitConverter.Family family = UnitConverter.familyOf(needed.getUnit());
        Map<UnitConverter.Family, Double> byFamily = stock.get(IngredientNormalizer.normalize(needed.getName()));
        if (family == null || byFamily == null) return false;
        Double have = byFamily.get(family);
        double required = UnitConverter.toBase(needed.getQuantity(), needed.getUnit());
        return have != null && have + EPSILON >= required;
    }
}
