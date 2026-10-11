package com.vysan.smartpantry.data.repo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vysan.smartpantry.data.db.DatabaseHelper;
import com.vysan.smartpantry.data.db.PantryDao;
import com.vysan.smartpantry.data.db.RecipeDao;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.model.Recipe;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for the UI. All database work runs on one background thread (so writes
 * happen in order and never block the UI), and results are published through LiveData.
 */
public final class PantryRepository {

    /** Receives the result of a one-off background operation on the main thread. */
    public interface Callback<T> {
        void onResult(T result);
    }

    private static volatile PantryRepository instance;

    private final PantryDao pantryDao;
    private final RecipeDao recipeDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<List<PantryItem>> pantryItems = new MutableLiveData<>();
    private final MutableLiveData<List<Recipe>> recipes = new MutableLiveData<>();

    private PantryRepository(Context context) {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        pantryDao = new PantryDao(helper);
        recipeDao = new RecipeDao(helper);
        executor.execute(() -> recipes.postValue(recipeDao.getAll()));
        reloadPantry();
    }

    public static PantryRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (PantryRepository.class) {
                if (instance == null) instance = new PantryRepository(context.getApplicationContext());
            }
        }
        return instance;
    }

    /** Live list of pantry items; updates automatically after every write. */
    public LiveData<List<PantryItem>> getPantryItems() {
        return pantryItems;
    }

    /** Live list of all seeded recipes with their ingredients. */
    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    public void insert(PantryItem item, Callback<Long> onDone) {
        executor.execute(() -> {
            long id = pantryDao.insert(item);
            pantryItems.postValue(pantryDao.getAll());
            deliver(onDone, id);
        });
    }

    public void update(PantryItem item, Callback<Integer> onDone) {
        executor.execute(() -> {
            int rows = pantryDao.update(item);
            pantryItems.postValue(pantryDao.getAll());
            deliver(onDone, rows);
        });
    }

    public void delete(PantryItem item) {
        executor.execute(() -> {
            pantryDao.delete(item.getId());
            pantryItems.postValue(pantryDao.getAll());
        });
    }

    /** Re-inserts an item removed moments ago (Undo on the delete Snackbar). */
    public void restore(PantryItem item) {
        executor.execute(() -> {
            pantryDao.restore(item);
            pantryItems.postValue(pantryDao.getAll());
        });
    }

    public void deleteAll() {
        executor.execute(() -> {
            pantryDao.deleteAll();
            pantryItems.postValue(pantryDao.getAll());
        });
    }

    public void loadItem(long id, Callback<PantryItem> onLoaded) {
        executor.execute(() -> deliver(onLoaded, pantryDao.getById(id)));
    }

    public void loadRecipe(long id, Callback<Recipe> onLoaded) {
        executor.execute(() -> deliver(onLoaded, recipeDao.getById(id)));
    }

    private void reloadPantry() {
        executor.execute(() -> pantryItems.postValue(pantryDao.getAll()));
    }

    // Callbacks touch views, so they must run on the main thread.
    private <T> void deliver(Callback<T> callback, T result) {
        if (callback != null) mainHandler.post(() -> callback.onResult(result));
    }
}
