package com.vysan.smartpantry.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities to a base unit per family (grams, millilitres, pieces) so pantry stock and
 * recipe requirements can be compared even when entered in different units. Units from different
 * families (e.g. pieces versus grams) are never converted into each other.
 */
public final class UnitConverter {

    /** A group of units that can be converted into each other. */
    public enum Family { MASS, VOLUME, COUNT }

    private static final double ONE = 1.0;
    private static final double GRAMS_PER_KG = 1000.0;
    private static final double GRAMS_PER_OZ = 28.3495;
    private static final double GRAMS_PER_LB = 453.592;
    private static final double ML_PER_LITRE = 1000.0;
    private static final double ML_PER_TSP = 5.0;
    private static final double ML_PER_TBSP = 15.0;
    private static final double ML_PER_CUP = 240.0;

    private static final Map<String, Family> FAMILY = new HashMap<>();
    private static final Map<String, Double> FACTOR = new HashMap<>();

    static {
        register(Family.MASS, ONE, "g", "gram", "grams", "gm");
        register(Family.MASS, GRAMS_PER_KG, "kg", "kilogram", "kilograms", "kgs");
        register(Family.MASS, GRAMS_PER_OZ, "oz", "ounce", "ounces");
        register(Family.MASS, GRAMS_PER_LB, "lb", "lbs", "pound", "pounds");
        register(Family.VOLUME, ONE, "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        register(Family.VOLUME, ML_PER_LITRE, "l", "litre", "litres", "liter", "liters");
        register(Family.VOLUME, ML_PER_TSP, "tsp", "teaspoon", "teaspoons");
        register(Family.VOLUME, ML_PER_TBSP, "tbsp", "tablespoon", "tablespoons");
        register(Family.VOLUME, ML_PER_CUP, "cup", "cups");
        register(Family.COUNT, ONE, "pcs", "pc", "piece", "pieces", "whole", "clove", "cloves",
                "slice", "slices");
    }

    private UnitConverter() {
    }

    private static void register(Family family, double factor, String... aliases) {
        for (String alias : aliases) {
            FAMILY.put(alias, family);
            FACTOR.put(alias, factor);
        }
    }

    private static String key(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
    }

    /** True when the unit (or one of its aliases) is recognised. */
    public static boolean isKnown(String unit) {
        return FAMILY.containsKey(key(unit));
    }

    /** The family a unit belongs to, or null when the unit is unknown. */
    public static Family familyOf(String unit) {
        return FAMILY.get(key(unit));
    }

    /**
     * Converts a quantity to its family's base unit (g, ml or pcs).
     *
     * @throws IllegalArgumentException if the unit is unknown, so bad data fails loudly instead of matching
     */
    public static double toBase(double quantity, String unit) {
        Double factor = FACTOR.get(key(unit));
        if (factor == null) throw new IllegalArgumentException("Unknown unit: " + unit);
        return quantity * factor;
    }
}
