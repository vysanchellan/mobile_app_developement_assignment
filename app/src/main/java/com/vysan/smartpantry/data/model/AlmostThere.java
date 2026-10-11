package com.vysan.smartpantry.data.model;

/**
 * A recipe the user cannot cook yet because exactly one ingredient is missing or short.
 * Kept as its own type so it can never be confused with a strict match.
 */
public final class AlmostThere {

    private final Recipe recipe;
    private final RecipeIngredient missing;

    public AlmostThere(Recipe recipe, RecipeIngredient missing) {
        this.recipe = recipe;
        this.missing = missing;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    /** The single ingredient that is absent from the pantry or present in too small a quantity. */
    public RecipeIngredient getMissing() {
        return missing;
    }
}
