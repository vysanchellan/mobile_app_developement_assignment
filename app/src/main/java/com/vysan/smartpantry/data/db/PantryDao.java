package com.vysan.smartpantry.data.db;

import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_CREATED_AT;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_EXPIRY_DATE;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_ID;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_NAME;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_NORMALIZED_NAME;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_QUANTITY;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_UNIT;
import static com.vysan.smartpantry.data.db.DatabaseHelper.TABLE_PANTRY;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.logic.IngredientNormalizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Create, read, update and delete for the pantry_items table. Every query is parameterised with
 * "?" placeholders, so user input is never concatenated into SQL. Call only from a background thread.
 */
public final class PantryDao {

    private static final String WHERE_ID = COL_ID + " = ?";

    /** Items with an expiry date first (soonest at the top), then the rest, each group by name. */
    private static final String ORDER_BY_EXPIRY = COL_EXPIRY_DATE + " IS NULL, "
            + COL_EXPIRY_DATE + " ASC, " + COL_NAME + " COLLATE NOCASE ASC";

    private final DatabaseHelper helper;

    public PantryDao(DatabaseHelper helper) {
        this.helper = helper;
    }

    /** CREATE: inserts a new item and returns its generated id (-1 on failure). */
    public long insert(PantryItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, toValues(item));
    }

    /** READ: every item, sorted by soonest expiry then name. */
    public List<PantryItem> getAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, ORDER_BY_EXPIRY)) {
            while (c.moveToNext()) items.add(fromCursor(c));
        }
        return items;
    }

    /** READ one: the item with this id, or null if it no longer exists. */
    public PantryItem getById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] args = {String.valueOf(id)};
        try (Cursor c = db.query(TABLE_PANTRY, null, WHERE_ID, args, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /** UPDATE: overwrites the row with the item's id; returns the number of rows changed. */
    public int update(PantryItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] args = {String.valueOf(item.getId())};
        return db.update(TABLE_PANTRY, toValues(item), WHERE_ID, args);
    }

    /** DELETE: removes one item; returns the number of rows deleted. */
    public int delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] args = {String.valueOf(id)};
        return db.delete(TABLE_PANTRY, WHERE_ID, args);
    }

    /** Puts a just-deleted item back with its original id, so Undo restores it exactly. */
    public long restore(PantryItem item) {
        ContentValues values = toValues(item);
        values.put(COL_ID, item.getId());
        return helper.getWritableDatabase().insert(TABLE_PANTRY, null, values);
    }

    /** Removes every pantry item (Settings > Reset pantry). Recipes are untouched. */
    public void deleteAll() {
        helper.getWritableDatabase().delete(TABLE_PANTRY, null, null);
    }

    // The normalised name is stored too, so lookups by ingredient can use the index.
    private static ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_NORMALIZED_NAME, IngredientNormalizer.normalize(item.getName()));
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY_DATE, item.getExpiryDate());
        values.put(COL_CREATED_AT, item.getCreatedAt());
        return values;
    }

    private static PantryItem fromCursor(Cursor c) {
        int expiryIndex = c.getColumnIndexOrThrow(COL_EXPIRY_DATE);
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(COL_UNIT)),
                c.isNull(expiryIndex) ? null : c.getString(expiryIndex),
                c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT)));
    }
}
