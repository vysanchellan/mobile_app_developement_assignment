package com.vysan.smartpantry.data.model;

import java.util.Objects;

/**
 * One row of the user's pantry: an ingredient, how much of it there is, and an optional expiry date.
 * Immutable so it can be safely shared between the background executor and the UI thread.
 */
public final class PantryItem {

    /** Id used for items that have not been saved to the database yet. */
    public static final long NO_ID = 0L;

    private final long id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate;
    private final long createdAt;

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate, long createdAt) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    /** ISO yyyy-MM-dd, or null when the user did not set an expiry date. */
    public String getExpiryDate() {
        return expiryDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    /** Returns a copy carrying the id the database assigned on insert. */
    public PantryItem withId(long newId) {
        return new PantryItem(newId, name, quantity, unit, expiryDate, createdAt);
    }

    // Value equality lets DiffUtil detect which rows actually changed.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PantryItem)) return false;
        PantryItem other = (PantryItem) o;
        return id == other.id
                && Double.compare(quantity, other.quantity) == 0
                && createdAt == other.createdAt
                && name.equals(other.name)
                && unit.equals(other.unit)
                && Objects.equals(expiryDate, other.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, quantity, unit, expiryDate, createdAt);
    }
}
