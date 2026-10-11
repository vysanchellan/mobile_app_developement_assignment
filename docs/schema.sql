-- Smart Pantry Manager database schema (smart_pantry.db, version 1).
-- Kept identical to the CREATE statements in DatabaseHelper.java.
-- Foreign keys are enabled in DatabaseHelper.onConfigure.

CREATE TABLE pantry_items (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  normalized_name TEXT NOT NULL,
  quantity REAL NOT NULL CHECK (quantity > 0),
  unit TEXT NOT NULL,
  expiry_date TEXT,              -- ISO yyyy-MM-dd, nullable
  created_at INTEGER NOT NULL
);

CREATE TABLE recipes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  description TEXT,
  prep_minutes INTEGER NOT NULL,
  servings INTEGER NOT NULL,
  steps TEXT NOT NULL            -- steps separated by a newline
);

CREATE TABLE recipe_ingredients (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  normalized_name TEXT NOT NULL,
  quantity REAL NOT NULL,
  unit TEXT NOT NULL
);

CREATE INDEX idx_pantry_norm ON pantry_items(normalized_name);
CREATE INDEX idx_ri_recipe ON recipe_ingredients(recipe_id);
