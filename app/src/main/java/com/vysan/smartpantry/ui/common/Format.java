package com.vysan.smartpantry.ui.common;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Small display helpers shared by several screens, so numbers and dates look the same everywhere.
 */
public final class Format {

    private static final String QUANTITY_PATTERN = "0.###";
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private Format() {
    }

    /** "6" instead of "6.0", "0.1" instead of "0.10000"; always a dot so it can be parsed back. */
    public static String quantity(double value) {
        return new DecimalFormat(QUANTITY_PATTERN, DecimalFormatSymbols.getInstance(Locale.ROOT)).format(value);
    }

    /** Converts a stored yyyy-MM-dd date to the user's locale, e.g. "12 Oct 2026". */
    public static String displayDate(String isoDate) {
        return LocalDate.parse(isoDate).format(DISPLAY_DATE);
    }
}
