package com.vysan.smartpantry.data.db;

import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_DESCRIPTION;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_ID;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_NAME;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_NORMALIZED_NAME;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_PREP_MINUTES;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_QUANTITY;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_RECIPE_ID;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_SERVINGS;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_STEPS;
import static com.vysan.smartpantry.data.db.DatabaseHelper.COL_UNIT;
import static com.vysan.smartpantry.data.db.DatabaseHelper.TABLE_RECIPES;
import static com.vysan.smartpantry.data.db.DatabaseHelper.TABLE_RECIPE_INGREDIENTS;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.model.RecipeIngredient;
import com.vysan.smartpantry.logic.IngredientNormalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads recipes with their ingredients, and inserts recipes during seeding.
 * Recipes are read-only for the user, so there is no update or delete here.
 */
public final class RecipeDao {

    /** Method steps are stored in one TEXT column, one step per line. */
    private static final String STEP_SEPARATOR = "\n";
    private static final String ORDER_BY_NAME = COL_NAME + " COLLATE NOCASE ASC";

    private final DatabaseHelper helper;

    public RecipeDao(DatabaseHelper helper) {
        this.helper = helper;
    }

    /** Inserts a recipe and its ingredient rows. Takes the raw db so it can run inside onCreate. */
    static void insert(SQLiteDatabase db, Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, recipe.getName());
        values.put(COL_DESCRIPTION, recipe.getDescription());
        values.put(COL_PREP_MINUTES, recipe.getPrepMinutes());
        values.put(COL_SERVINGS, recipe.getServings());
        values.put(COL_STEPS, String.join(STEP_SEPARATOR, recipe.getSteps()));
        long recipeId = db.insertOrThrow(TABLE_RECIPES, null, values);

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ContentValues row = new ContentValues();
            row.put(COL_RECIPE_ID, recipeId);
            row.put(COL_NAME, ingredient.getName());
            row.put(COL_NORMALIZED_NAME, IngredientNormalizer.normalize(ingredient.getName()));
            row.put(COL_QUANTITY, ingredient.getQuantity());
            row.put(COL_UNIT, ingredient.getUnit());
            db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, row);
        }
    }

    /** All recipes with their ingredients, loaded in two queries instead of one per recipe. */
    public List<Recipe> getAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Map<Long, List<RecipeIngredient>> ingredients = loadIngredients(db, null, null);
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, ORDER_BY_NAME)) {
            while (c.moveToNext()) recipes.add(fromCursor(c, ingredients));
        }
        return recipes;
    }

    /** One recipe with its ingredients, or null if the id is unknown. */
    public Recipe getById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] args = {String.valueOf(id)};
        Map<Long, List<RecipeIngredient>> ingredients = loadIngredients(db, COL_RECIPE_ID + " = ?", args);
        try (Cursor c = db.query(TABLE_RECIPES, null, COL_ID + " = ?", args, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c, ingredients) : null;
        }
    }

    // Groups ingredient rows by recipe id, preserving the order they were inserted in.
    private static Map<Long, List<RecipeIngredient>> loadIngredients(SQLiteDatabase db, String where, String[] args) {
        Map<Long, List<RecipeIngredient>> byRecipe = new HashMap<>();
        try (Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, where, args, null, null, COL_ID)) {
            while (c.moveToNext()) {
                long recipeId = c.getLong(c.getColumnIndexOrThrow(COL_RECIPE_ID));
                RecipeIngredient ingredient = new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)),
                        c.getString(c.getColumnIndexOrThrow(COL_UNIT)));
                byRecipe.computeIfAbsent(recipeId, k -> new ArrayList<>()).add(ingredient);
            }
        }
        return byRecipe;
    }

    private static Recipe fromCursor(Cursor c, Map<Long, List<RecipeIngredient>> ingredients) {
        long id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
        String steps = c.getString(c.getColumnIndexOrThrow(COL_STEPS));
        List<RecipeIngredient> list = ingredients.get(id);
        return new Recipe(
                id,
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)),
                c.getInt(c.getColumnIndexOrThrow(COL_PREP_MINUTES)),
                c.getInt(c.getColumnIndexOrThrow(COL_SERVINGS)),
                Arrays.asList(steps.split(STEP_SEPARATOR)),
                list == null ? new ArrayList<>() : list);
    }
}
