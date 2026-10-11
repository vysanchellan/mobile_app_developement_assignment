package com.vysan.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.vysan.smartpantry.data.model.AlmostThere;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Unit tests for the strict matching rule and the separate "almost there" list. */
public class RecipeMatcherTest {

    private final RecipeMatcher matcher = new RecipeMatcher();

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(1, name, quantity, unit, null, 0);
    }

    private static RecipeIngredient need(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        return new Recipe(1, name, "", 10, 1, Collections.singletonList("Cook."), Arrays.asList(ingredients));
    }

    private static final Recipe FIVE_ITEM_RECIPE = recipe("Five",
            need("Eggs", 2, "pcs"), need("Milk", 100, "ml"), need("Flour", 200, "g"),
            need("Sugar", 1, "tbsp"), need("Butter", 20, "g"));

    private List<Recipe> strict(List<PantryItem> pantry, Recipe... recipes) {
        return matcher.findStrictMatches(pantry, Arrays.asList(recipes));
    }

    @Test
    public void fourOfFiveIngredientsIsExcluded() {
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 6, "pcs"), item("Milk", 1, "l"),
                item("Flour", 1, "kg"), item("Sugar", 500, "g"));
        assertTrue(strict(pantry, FIVE_ITEM_RECIPE).isEmpty());
    }

    @Test
    public void allFiveIngredientsIsIncluded() {
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 6, "pcs"), item("Milk", 1, "l"),
                item("Flour", 1, "kg"), item("Sugar", 5, "tbsp"), item("Butter", 250, "g"));
        assertEquals(1, strict(pantry, FIVE_ITEM_RECIPE).size());
    }

    @Test
    public void exactQuantityIsEnough() {
        Recipe r = recipe("Exact", need("Butter", 30, "g"));
        assertEquals(1, strict(Collections.singletonList(item("Butter", 30, "g")), r).size());
    }

    @Test
    public void quantityShortByAHairIsExcluded() {
        Recipe r = recipe("Hair", need("Butter", 30, "g"));
        assertTrue(strict(Collections.singletonList(item("Butter", 29.999, "g")), r).isEmpty());
    }

    @Test
    public void floatingPointRoundingDoesNotExclude() {
        // 1.2 + 2.95 tbsp sums to 62.25 ml, but 4.15 tbsp converts to 62.25000000000001 ml.
        Recipe r = recipe("Float", need("Olive oil", 4.15, "tbsp"));
        List<PantryItem> pantry = Arrays.asList(item("Olive oil", 1.2, "tbsp"), item("Olive oil", 2.95, "tbsp"));
        assertEquals(1, strict(pantry, r).size());
    }

    @Test
    public void pluralAndSingularNamesMatch() {
        Recipe r = recipe("Tomato", need("tomato", 1, "pcs"));
        assertEquals(1, strict(Collections.singletonList(item("Tomatoes", 2, "pcs")), r).size());
    }

    @Test
    public void caseAndWhitespaceNoiseIsIgnored() {
        Recipe r = recipe("Cheese", need("Cheddar cheese", 30, "g"));
        assertEquals(1, strict(Collections.singletonList(item("  CHEDDAR   cheese ", 30, "g")), r).size());
    }

    @Test
    public void convertsKilogramsBeforeComparing() {
        Recipe r = recipe("Cheese", need("Cheddar cheese", 30, "g"));
        assertEquals(1, strict(Collections.singletonList(item("Cheddar cheese", 0.1, "kg")), r).size());
    }

    @Test
    public void duplicateRowsAreSummed() {
        Recipe r = recipe("Butter", need("Butter", 30, "g"));
        List<PantryItem> pantry = Arrays.asList(item("Butter", 20, "g"), item("butter", 0.015, "kg"));
        assertEquals(1, strict(pantry, r).size());
    }

    @Test
    public void crossFamilyMismatchDoesNotSatisfy() {
        Recipe r = recipe("Cheese", need("Cheese", 200, "g"));
        assertTrue(strict(Collections.singletonList(item("Cheese", 2, "pcs")), r).isEmpty());
    }

    @Test
    public void unknownUnitNeverSatisfies() {
        Recipe r = recipe("Herbs", need("Parsley", 1, "pcs"));
        assertTrue(strict(Collections.singletonList(item("Parsley", 1, "handful")), r).isEmpty());
    }

    @Test
    public void emptyPantryReturnsEmpty() {
        assertTrue(strict(new ArrayList<>(), FIVE_ITEM_RECIPE).isEmpty());
    }

    @Test
    public void recipeWithNoIngredientsIsNeverSuggested() {
        assertTrue(strict(Collections.singletonList(item("Eggs", 1, "pcs")), recipe("Nothing")).isEmpty());
    }

    @Test
    public void expiredItemsStillCount() {
        PantryItem expired = new PantryItem(1, "Eggs", 6, "pcs", "2000-01-01", 0);
        Recipe r = recipe("Eggs", need("Eggs", 2, "pcs"));
        assertEquals(1, strict(Collections.singletonList(expired), r).size());
    }

    @Test
    public void almostThereNamesTheSingleMissingIngredient() {
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 6, "pcs"), item("Milk", 1, "l"),
                item("Flour", 1, "kg"), item("Sugar", 5, "tbsp"));
        List<AlmostThere> almost = matcher.findAlmostThere(pantry, Collections.singletonList(FIVE_ITEM_RECIPE));
        assertEquals(1, almost.size());
        assertEquals("Butter", almost.get(0).getMissing().getName());
    }

    @Test
    public void almostThereCountsAShortQuantityAsTheMissingItem() {
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 1, "pcs"), item("Milk", 1, "l"),
                item("Flour", 1, "kg"), item("Sugar", 5, "tbsp"), item("Butter", 250, "g"));
        List<AlmostThere> almost = matcher.findAlmostThere(pantry, Collections.singletonList(FIVE_ITEM_RECIPE));
        assertEquals(1, almost.size());
        assertEquals("Eggs", almost.get(0).getMissing().getName());
    }

    @Test
    public void almostThereExcludesTwoMissingAndFullMatches() {
        List<PantryItem> threeOfFive = Arrays.asList(item("Eggs", 6, "pcs"), item("Milk", 1, "l"),
                item("Flour", 1, "kg"));
        assertTrue(matcher.findAlmostThere(threeOfFive, Collections.singletonList(FIVE_ITEM_RECIPE)).isEmpty());

        List<PantryItem> all = Arrays.asList(item("Eggs", 6, "pcs"), item("Milk", 1, "l"),
                item("Flour", 1, "kg"), item("Sugar", 5, "tbsp"), item("Butter", 250, "g"));
        assertTrue(matcher.findAlmostThere(all, Collections.singletonList(FIVE_ITEM_RECIPE)).isEmpty());
    }

    @Test
    public void findShortfallsListsEveryUncoveredIngredient() {
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 6, "pcs"), item("Milk", 1, "l"),
                item("Flour", 1, "kg"));
        assertEquals(2, matcher.findShortfalls(pantry, FIVE_ITEM_RECIPE).size());
    }
}
