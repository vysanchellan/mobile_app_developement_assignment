package com.vysan.smartpantry.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for name normalisation: case, whitespace, punctuation, plurals and synonyms. */
public class IngredientNormalizerTest {

    @Test
    public void lowercasesTrimsAndCollapsesWhitespace() {
        assertEquals("cheddar cheese", IngredientNormalizer.normalize("  Cheddar    CHEESE "));
    }

    @Test
    public void stripsPunctuation() {
        assertEquals("egg", IngredientNormalizer.normalize("Eggs!"));
        assertEquals("stir fry sauce", IngredientNormalizer.normalize("stir-fry sauce"));
    }

    @Test
    public void singularisesCommonPlurals() {
        assertEquals("tomato", IngredientNormalizer.normalize("Tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("berry", IngredientNormalizer.normalize("berries"));
        assertEquals("egg", IngredientNormalizer.normalize("eggs"));
        assertEquals("clove", IngredientNormalizer.normalize("cloves"));
        assertEquals("peach", IngredientNormalizer.normalize("peaches"));
        assertEquals("lentil", IngredientNormalizer.normalize("lentils"));
    }

    @Test
    public void handlesIrregularPlurals() {
        assertEquals("bay leaf", IngredientNormalizer.normalize("Bay leaves"));
        assertEquals("loaf", IngredientNormalizer.normalize("loaves"));
    }

    @Test
    public void leavesInvariantWordsAlone() {
        assertEquals("asparagus", IngredientNormalizer.normalize("Asparagus"));
        assertEquals("hummus", IngredientNormalizer.normalize("hummus"));
        assertEquals("couscous", IngredientNormalizer.normalize("couscous"));
        assertEquals("molasses", IngredientNormalizer.normalize("molasses"));
    }

    @Test
    public void singularisesOnlyTheHeadNounOfAPhrase() {
        assertEquals("cherry tomato", IngredientNormalizer.normalize("Cherry Tomatoes"));
    }

    @Test
    public void mapsSynonymsAfterSingularising() {
        assertEquals("bell pepper", IngredientNormalizer.normalize("Capsicums"));
        assertEquals("zucchini", IngredientNormalizer.normalize("courgette"));
        assertEquals("eggplant", IngredientNormalizer.normalize("Aubergines"));
        assertEquals("garlic", IngredientNormalizer.normalize("garlic cloves"));
        assertEquals("cheddar cheese", IngredientNormalizer.normalize("Cheddar"));
    }

    @Test
    public void nullAndBlankBecomeEmpty() {
        assertEquals("", IngredientNormalizer.normalize(null));
        assertEquals("", IngredientNormalizer.normalize("   "));
        assertEquals("", IngredientNormalizer.normalize("!!!"));
    }
}
