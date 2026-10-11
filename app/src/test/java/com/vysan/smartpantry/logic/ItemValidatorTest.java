package com.vysan.smartpantry.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDate;

/** Unit tests for the Add/Edit form validation rules. */
public class ItemValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    @Test
    public void nameIsRequired() {
        assertEquals(ItemValidator.Result.EMPTY, ItemValidator.validateName(""));
        assertEquals(ItemValidator.Result.EMPTY, ItemValidator.validateName("   "));
        assertEquals(ItemValidator.Result.EMPTY, ItemValidator.validateName(null));
    }

    @Test
    public void nameAllowsLettersSpacesAndHyphens() {
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateName("Cheddar cheese"));
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateName("Stir-fry sauce"));
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateName("Crème fraîche"));
    }

    @Test
    public void nameRejectsDigitsAndSymbols() {
        assertEquals(ItemValidator.Result.INVALID, ItemValidator.validateName("Eggs 2"));
        assertEquals(ItemValidator.Result.INVALID, ItemValidator.validateName("-eggs"));
        assertEquals(ItemValidator.Result.INVALID, ItemValidator.validateName("eggs;drop"));
    }

    @Test
    public void nameLongerThanFortyIsRejected() {
        String forty = "abcdefghijabcdefghijabcdefghijabcdefghij";
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateName(forty));
        assertEquals(ItemValidator.Result.TOO_LONG, ItemValidator.validateName(forty + "k"));
    }

    @Test
    public void quantityMustBeAPositiveNumberWithinRange() {
        assertEquals(ItemValidator.Result.EMPTY, ItemValidator.validateQuantity(""));
        assertEquals(ItemValidator.Result.NOT_A_NUMBER, ItemValidator.validateQuantity("abc"));
        assertEquals(ItemValidator.Result.NOT_A_NUMBER, ItemValidator.validateQuantity("1e5"));
        assertEquals(ItemValidator.Result.NOT_A_NUMBER, ItemValidator.validateQuantity("NaN"));
        assertEquals(ItemValidator.Result.NOT_POSITIVE, ItemValidator.validateQuantity("0"));
        assertEquals(ItemValidator.Result.NOT_POSITIVE, ItemValidator.validateQuantity("0.0"));
        assertEquals(ItemValidator.Result.TOO_LARGE, ItemValidator.validateQuantity("100000.5"));
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateQuantity("100000"));
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateQuantity("0.1"));
    }

    @Test
    public void quantityAcceptsDecimalComma() {
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateQuantity("0,5"));
        assertEquals(0.5, ItemValidator.parseQuantity("0,5"), 1e-9);
    }

    @Test
    public void unitMustBeKnown() {
        assertEquals(ItemValidator.Result.EMPTY, ItemValidator.validateUnit(""));
        assertEquals(ItemValidator.Result.INVALID, ItemValidator.validateUnit("handful"));
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateUnit("tbsp"));
    }

    @Test
    public void newItemCannotBeAddedAlreadyExpired() {
        assertEquals(ItemValidator.Result.IN_PAST, ItemValidator.validateExpiry("2026-03-09", TODAY, true));
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateExpiry("2026-03-10", TODAY, true));
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateExpiry(null, TODAY, true));
    }

    @Test
    public void editingAnExpiredItemIsAllowed() {
        assertEquals(ItemValidator.Result.OK, ItemValidator.validateExpiry("2026-03-01", TODAY, false));
    }

    @Test
    public void cleanNameCollapsesSpaces() {
        assertEquals("Cheddar cheese", ItemValidator.cleanName("  Cheddar    cheese "));
    }
}
