package com.vysan.smartpantry.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.vysan.smartpantry.data.model.AlmostThere;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.model.RecipeIngredient;
import com.vysan.smartpantry.logic.IngredientNormalizer;
import com.vysan.smartpantry.logic.RecipeMatcher;
import com.vysan.smartpantry.logic.UnitConverter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifies the seed recipes and the exact scenario shown in the video demo:
 * Eggs 6 pcs, Tomatoes 2 pcs, Cheddar Cheese 0.1 kg, Butter 50 g.
 */
public class SeedDataTest {

    private static final String OMELETTE = "Cheesy Tomato Omelette";
    private static final int SEED_COUNT = 20;

    private final RecipeMatcher matcher = new RecipeMatcher();
    private final List<Recipe> recipes = SeedData.recipes();

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(1, name, quantity, unit, null, 0);
    }

    private static List<PantryItem> demoPantry(boolean withButter, double eggs) {
        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(
                item("Eggs", eggs, "pcs"), item("Tomatoes", 2, "pcs"), item("Cheddar Cheese", 0.1, "kg")));
        if (withButter) pantry.add(item("Butter", 50, "g"));
        return pantry;
    }

    @Test
    public void seedsTwentyRecipesWithSensibleSizes() {
        assertEquals(SEED_COUNT, recipes.size());
        for (Recipe r : recipes) {
            int ingredients = r.getIngredients().size();
            int steps = r.getSteps().size();
            assertTrue(r.getName(), ingredients >= 3 && ingredients <= 6);
            assertTrue(r.getName(), steps >= 4 && steps <= 7);
        }
    }

    @Test
    public void everySeedIngredientUsesAKnownUnitAndVocabularyName() {
        Set<String> vocabulary = new HashSet<>();
        for (String name : SeedData.INGREDIENT_VOCABULARY) vocabulary.add(IngredientNormalizer.normalize(name));
        for (Recipe r : recipes) {
            for (RecipeIngredient i : r.getIngredients()) {
                assertTrue(i.getUnit(), UnitConverter.isKnown(i.getUnit()));
                assertTrue(i.getName(), vocabulary.contains(IngredientNormalizer.normalize(i.getName())));
            }
        }
    }

    @Test
    public void demoPantryMatchesExactlyTheOmelette() {
        List<Recipe> strict = matcher.findStrictMatches(demoPantry(true, 6), recipes);
        assertEquals(1, strict.size());
        assertEquals(OMELETTE, strict.get(0).getName());
    }

    @Test
    public void withoutButterTheOmeletteIsOnlyAlmostThere() {
        List<PantryItem> pantry = demoPantry(false, 6);
        assertTrue(matcher.findStrictMatches(pantry, recipes).isEmpty());

        List<AlmostThere> almost = matcher.findAlmostThere(pantry, recipes);
        assertEquals(1, almost.size());
        assertEquals(OMELETTE, almost.get(0).getRecipe().getName());
        assertEquals("butter", IngredientNormalizer.normalize(almost.get(0).getMissing().getName()));
    }

    @Test
    public void twoEggsAreNotEnoughForTheOmelette() {
        assertTrue(matcher.findStrictMatches(demoPantry(true, 2), recipes).isEmpty());
    }
}
