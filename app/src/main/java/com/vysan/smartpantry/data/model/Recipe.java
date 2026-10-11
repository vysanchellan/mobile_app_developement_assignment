package com.vysan.smartpantry.data.model;

import java.util.Collections;
import java.util.List;

/**
 * A recipe with its required ingredients and ordered method steps.
 */
public final class Recipe {

    private final long id;
    private final String name;
    private final String description;
    private final int prepMinutes;
    private final int servings;
    private final List<String> steps;
    private final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String description, int prepMinutes, int servings,
                  List<String> steps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prepMinutes = prepMinutes;
        this.servings = servings;
        this.steps = Collections.unmodifiableList(steps);
        this.ingredients = Collections.unmodifiableList(ingredients);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrepMinutes() {
        return prepMinutes;
    }

    public int getServings() {
        return servings;
    }

    public List<String> getSteps() {
        return steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }
}
