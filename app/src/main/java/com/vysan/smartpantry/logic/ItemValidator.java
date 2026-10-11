package com.vysan.smartpantry.logic;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validation rules for the Add/Edit ingredient form. Pure Java so every rule is unit tested;
 * the activity only maps each {@link Result} to an error message.
 */
public final class ItemValidator {

    /** Outcome of validating one field. */
    public enum Result { OK, EMPTY, TOO_LONG, INVALID, NOT_A_NUMBER, NOT_POSITIVE, TOO_LARGE, IN_PAST }

    public static final int MAX_NAME_LENGTH = 40;
    public static final double MAX_QUANTITY = 100_000;

    /** Starts with a letter, then letters, spaces or hyphens (any alphabet, e.g. "Crème fraîche"). */
    private static final Pattern NAME = Pattern.compile("\\p{L}[\\p{L} \\-]*");
    /** Plain decimals only: "2", "0.5", ".5". Rejects "1e5", "NaN" and "Infinity" that parseDouble accepts. */
    private static final Pattern DECIMAL = Pattern.compile("\\d+(\\.\\d*)?|\\.\\d+");

    private ItemValidator() {
    }

    public static Result validateName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) return Result.EMPTY;
        if (name.length() > MAX_NAME_LENGTH) return Result.TOO_LONG;
        if (!NAME.matcher(name).matches()) return Result.INVALID;
        return Result.OK;
    }

    public static Result validateQuantity(String raw) {
        String text = normaliseDecimal(raw);
        if (text.isEmpty()) return Result.EMPTY;
        if (!DECIMAL.matcher(text).matches()) return Result.NOT_A_NUMBER;
        double value = Double.parseDouble(text);
        if (value <= 0) return Result.NOT_POSITIVE;
        if (value > MAX_QUANTITY) return Result.TOO_LARGE;
        return Result.OK;
    }

    /** Parses a quantity that already passed {@link #validateQuantity}. */
    public static double parseQuantity(String raw) {
        return Double.parseDouble(normaliseDecimal(raw));
    }

    public static Result validateUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) return Result.EMPTY;
        return UnitConverter.isKnown(unit) ? Result.OK : Result.INVALID;
    }

    /** Expiry is optional; a new item may not be added already expired. */
    public static Result validateExpiry(String isoDate, LocalDate today, boolean isNewItem) {
        if (isoDate == null || isoDate.isEmpty()) return Result.OK;
        try {
            LocalDate expiry = LocalDate.parse(isoDate);
            return isNewItem && expiry.isBefore(today) ? Result.IN_PAST : Result.OK;
        } catch (DateTimeParseException e) {
            return Result.INVALID;
        }
    }

    /** Collapses repeated spaces so "Cheddar   cheese" is stored tidily. */
    public static String cleanName(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }

    // Many South African keyboards type a decimal comma, so "0,5" is accepted as 0.5.
    private static String normaliseDecimal(String raw) {
        return raw == null ? "" : raw.trim().replace(',', '.');
    }
}
