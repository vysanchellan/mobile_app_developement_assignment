package com.vysan.smartpantry.ui.common;

import android.os.SystemClock;

/**
 * Ignores a second tap that arrives too soon after the first, so a fast double tap cannot open
 * the same screen twice. Shared app-wide because only one screen can be opened at a time.
 */
public final class ClickGuard {

    /** Long enough to swallow an accidental double tap, short enough not to be noticed. */
    private static final long MIN_INTERVAL_MS = 600;

    private static long lastAccepted;

    private ClickGuard() {
    }

    /** Returns true if this tap should be handled. Call only on the main thread. */
    public static boolean allow() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastAccepted < MIN_INTERVAL_MS) return false;
        lastAccepted = now;
        return true;
    }
}
