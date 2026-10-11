package com.vysan.smartpantry.logic;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns free-text ingredient names into one canonical key so "Tomatoes", " tomato " and "TOMATO!"
 * are all treated as the same ingredient by the matcher. Pure Java so it can be unit tested.
 */
public final class IngredientNormalizer {

    /** Shortest word we try to singularise; avoids mangling short words like "gas" or "bus". */
    private static final int MIN_PLURAL_LENGTH = 4;

    /** Words that end in "s" but are already singular (or have no singular in cooking use). */
    private static final Set<String> INVARIANT = Set.of(
            "asparagus", "hummus", "couscous", "molasses", "citrus", "swiss");

    /** Irregular plurals the suffix rules below would get wrong. */
    private static final Map<String, String> IRREGULAR = Map.of(
            "leaves", "leaf",
            "loaves", "loaf",
            "halves", "half",
            "knives", "knife");

    /** Regional names mapped to the name the seed recipes use, applied after singularising. */
    private static final Map<String, String> SYNONYMS = Map.of(
            "capsicum", "bell pepper",
            "courgette", "zucchini",
            "aubergine", "eggplant",
            "mince", "beef mince",
            "cheddar", "cheddar cheese",
            "garlic clove", "garlic",
            "yogurt", "yoghurt");

    private IngredientNormalizer() {
    }

    /**
     * Lowercases, strips punctuation, collapses spaces, singularises the last word and applies synonyms.
     * Returns an empty string for null or blank input.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String cleaned = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) return "";

        // Only the head noun is plural in phrases like "cherry tomatoes" or "garlic cloves".
        int lastSpace = cleaned.lastIndexOf(' ');
        String head = cleaned.substring(lastSpace + 1);
        String phrase = cleaned.substring(0, lastSpace + 1) + singularize(head);
        return SYNONYMS.getOrDefault(phrase, phrase);
    }

    /** Converts one lowercase English word to its singular form using simple, documented rules. */
    static String singularize(String word) {
        if (word.length() < MIN_PLURAL_LENGTH || INVARIANT.contains(word)) return word;
        String irregular = IRREGULAR.get(word);
        if (irregular != null) return irregular;

        if (word.endsWith("ies")) return word.substring(0, word.length() - 3) + "y";   // berries -> berry
        if (word.endsWith("oes")) return word.substring(0, word.length() - 2);         // tomatoes -> tomato
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("sses")
                || word.endsWith("xes") || word.endsWith("zes")) {
            return word.substring(0, word.length() - 2);                                // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) return word;
        if (word.endsWith("s")) return word.substring(0, word.length() - 1);           // eggs -> egg
        return word;
    }
}
