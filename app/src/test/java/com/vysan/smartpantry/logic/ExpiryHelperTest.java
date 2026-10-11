package com.vysan.smartpantry.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDate;

/** Unit tests for expiry classification with a fixed "today". */
public class ExpiryHelperTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);
    private static final int WINDOW = 3;

    @Test
    public void noDateMeansNoStatus() {
        assertEquals(ExpiryHelper.Status.NONE, ExpiryHelper.statusOf(null, TODAY, WINDOW));
        assertEquals(ExpiryHelper.Status.NONE, ExpiryHelper.statusOf("", TODAY, WINDOW));
        assertEquals(ExpiryHelper.Status.NONE, ExpiryHelper.statusOf("not-a-date", TODAY, WINDOW));
    }

    @Test
    public void pastDateIsExpired() {
        assertEquals(ExpiryHelper.Status.EXPIRED, ExpiryHelper.statusOf("2026-03-09", TODAY, WINDOW));
    }

    @Test
    public void todayAndInsideWindowAreSoon() {
        assertEquals(ExpiryHelper.Status.SOON, ExpiryHelper.statusOf("2026-03-10", TODAY, WINDOW));
        assertEquals(ExpiryHelper.Status.SOON, ExpiryHelper.statusOf("2026-03-13", TODAY, WINDOW));
    }

    @Test
    public void beyondWindowIsFresh() {
        assertEquals(ExpiryHelper.Status.FRESH, ExpiryHelper.statusOf("2026-03-14", TODAY, WINDOW));
    }

    @Test
    public void daysUntilCountsCalendarDays() {
        assertEquals(1, ExpiryHelper.daysUntil("2026-03-11", TODAY));
        assertEquals(-2, ExpiryHelper.daysUntil("2026-03-08", TODAY));
    }
}
