package com.vysan.smartpantry.ui.recipes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vysan.smartpantry.data.model.AlmostThere;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.repo.PantryRepository;
import com.vysan.smartpantry.logic.IngredientNormalizer;
import com.vysan.smartpantry.logic.RecipeMatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Combines the live pantry and the recipe list and re-runs the matcher whenever either changes,
 * so the Recipes tab is always in step with the pantry. Also remembers the selected tab.
 */
public class RecipesViewModel extends AndroidViewModel {

    public static final int TAB_READY = 0;
    public static final int TAB_ALMOST = 1;

    /** Output of one matching run. Ready and almost-there lists never share a recipe. */
    public static final class MatchResult {
        public final List<Recipe> ready;
        public final List<AlmostThere> almost;
        /** Normalised names in the pantry, used to word a shortfall as "need more" rather than "missing". */
        public final Set<String> stockedNames;

        MatchResult(List<Recipe> ready, List<AlmostThere> almost, Set<String> stockedNames) {
            this.ready = ready;
            this.almost = almost;
            this.stockedNames = stockedNames;
        }
    }

    private final RecipeMatcher matcher = new RecipeMatcher();
    private final MediatorLiveData<MatchResult> result = new MediatorLiveData<>();
    private int selectedTab = TAB_READY;

    public RecipesViewModel(@NonNull Application application) {
        super(application);
        PantryRepository repository = PantryRepository.getInstance(application);
        LiveData<List<PantryItem>> pantry = repository.getPantryItems();
        LiveData<List<Recipe>> recipes = repository.getRecipes();
        result.addSource(pantry, items -> recompute(items, recipes.getValue()));
        result.addSource(recipes, list -> recompute(pantry.getValue(), list));
    }

    public LiveData<MatchResult> getResult() {
        return result;
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(int tab) {
        selectedTab = tab;
    }

    // Matching 20 recipes is pure in-memory work (no database), so it is cheap enough for the main thread.
    private void recompute(List<PantryItem> pantry, List<Recipe> recipes) {
        if (pantry == null || recipes == null) return;   // wait until both have loaded
        Set<String> stocked = new HashSet<>();
        for (PantryItem item : pantry) stocked.add(IngredientNormalizer.normalize(item.getName()));
        result.setValue(new MatchResult(
                matcher.findStrictMatches(pantry, recipes),
                matcher.findAlmostThere(pantry, recipes),
                stocked));
    }
}
