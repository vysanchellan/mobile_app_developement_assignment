package com.vysan.smartpantry.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vysan.smartpantry.data.model.Recipe;

/**
 * Owns the SQLite database file (smart_pantry.db): creates the three tables and seeds the recipes on first run.
 * The SQL below is kept identical to docs/schema.sql. One shared instance is used app-wide.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_NORMALIZED_NAME = "normalized_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY_DATE = "expiry_date";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PREP_MINUTES = "prep_minutes";
    public static final String COL_SERVINGS = "servings";
    public static final String COL_STEPS = "steps";
    public static final String COL_RECIPE_ID = "recipe_id";

    static final String SQL_CREATE_PANTRY = "CREATE TABLE pantry_items ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name TEXT NOT NULL, "
            + "normalized_name TEXT NOT NULL, "
            + "quantity REAL NOT NULL CHECK (quantity > 0), "
            + "unit TEXT NOT NULL, "
            + "expiry_date TEXT, "
            + "created_at INTEGER NOT NULL)";

    static final String SQL_CREATE_RECIPES = "CREATE TABLE recipes ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name TEXT NOT NULL, "
            + "description TEXT, "
            + "prep_minutes INTEGER NOT NULL, "
            + "servings INTEGER NOT NULL, "
            + "steps TEXT NOT NULL)";

    static final String SQL_CREATE_RECIPE_INGREDIENTS = "CREATE TABLE recipe_ingredients ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE, "
            + "name TEXT NOT NULL, "
            + "normalized_name TEXT NOT NULL, "
            + "quantity REAL NOT NULL, "
            + "unit TEXT NOT NULL)";

    static final String SQL_INDEX_PANTRY = "CREATE INDEX idx_pantry_norm ON pantry_items(normalized_name)";
    static final String SQL_INDEX_RECIPE = "CREATE INDEX idx_ri_recipe ON recipe_ingredients(recipe_id)";

    private static volatile DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Single shared helper so every DAO uses the same connection. */
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
            }
        }
        return instance;
    }

    // Foreign keys are off by default in SQLite; turn them on so ON DELETE CASCADE works.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_PANTRY);
            db.execSQL(SQL_CREATE_RECIPES);
            db.execSQL(SQL_CREATE_RECIPE_INGREDIENTS);
            db.execSQL(SQL_INDEX_PANTRY);
            db.execSQL(SQL_INDEX_RECIPE);
            for (Recipe recipe : SeedData.recipes()) {
                RecipeDao.insert(db, recipe);   // 20 seed recipes, all or nothing
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Only version 1 exists. A future version must migrate here instead of dropping user data.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }
}
