package com.vysan.smartpantry.logic;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Classifies an item's expiry date relative to today. Expired items still count for matching;
 * this class only decides how they are flagged in the UI.
 */
public final class ExpiryHelper {

    /** How an expiry date should be shown. */
    public enum Status { NONE, FRESH, SOON, EXPIRED }

    private ExpiryHelper() {
    }

    /**
     * @param isoDate    yyyy-MM-dd or null
     * @param today      injected so tests do not depend on the real clock
     * @param soonWindow number of days ahead that counts as "expiring soon"
     */
    public static Status statusOf(String isoDate, LocalDate today, int soonWindow) {
        LocalDate expiry = parse(isoDate);
        if (expiry == null) return Status.NONE;
        long days = ChronoUnit.DAYS.between(today, expiry);
        if (days < 0) return Status.EXPIRED;
        if (days <= soonWindow) return Status.SOON;
        return Status.FRESH;
    }

    /** Days from today until expiry (negative once expired). Only meaningful when status is not NONE. */
    public static long daysUntil(String isoDate, LocalDate today) {
        LocalDate expiry = parse(isoDate);
        return expiry == null ? 0 : ChronoUnit.DAYS.between(today, expiry);
    }

    // Treats bad or missing data as "no expiry date" instead of crashing the list.
    private static LocalDate parse(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return null;
        try {
            return LocalDate.parse(isoDate);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
