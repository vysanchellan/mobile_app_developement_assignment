package com.vysan.smartpantry.data.model;

/**
 * One ingredient line of a recipe, e.g. "3 pcs Eggs". Quantities are stored in the unit the recipe uses.
 */
public final class RecipeIngredient {

    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
