package com.vysan.smartpantry.ui.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.repo.PantryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Holds the pantry list and the search text for PantryFragment. Survives rotation, so neither the
 * list nor the user's search is lost when the screen turns.
 */
public class PantryViewModel extends AndroidViewModel {

    private final PantryRepository repository;
    private final MediatorLiveData<List<PantryItem>> visibleItems = new MediatorLiveData<>();
    private String query = "";

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = PantryRepository.getInstance(application);
        visibleItems.addSource(repository.getPantryItems(), items -> applyFilter());
    }

    /** Every pantry item (drives the summary banner and the "pantry is empty" state). */
    public LiveData<List<PantryItem>> getItems() {
        return repository.getPantryItems();
    }

    /** Items whose name contains the search text; all items when the search is empty. */
    public LiveData<List<PantryItem>> getVisibleItems() {
        return visibleItems;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String text) {
        query = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        applyFilter();
    }

    public void delete(PantryItem item) {
        repository.delete(item.getId());
    }

    /** Puts back an item deleted by swipe, keeping its original id. */
    public void restore(PantryItem item) {
        repository.restore(item);
    }

    // Filtering in memory is instant for a pantry-sized list and needs no extra query.
    private void applyFilter() {
        List<PantryItem> all = repository.getPantryItems().getValue();
        if (all == null) return;
        if (query.isEmpty()) {
            visibleItems.setValue(all);
            return;
        }
        List<PantryItem> matches = new ArrayList<>();
        for (PantryItem item : all) {
            if (item.getName().toLowerCase(Locale.ROOT).contains(query)) matches.add(item);
        }
        visibleItems.setValue(matches);
    }
}
