package com.vysan.smartpantry.ui.pantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.repo.PantryRepository;

import java.util.List;

/**
 * Holds the pantry list for PantryFragment. Survives rotation, so the list is not reloaded.
 */
public class PantryViewModel extends AndroidViewModel {

    private final PantryRepository repository;

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = PantryRepository.getInstance(application);
    }

    public LiveData<List<PantryItem>> getItems() {
        return repository.getPantryItems();
    }

    public void delete(PantryItem item) {
        repository.delete(item.getId());
    }

    /** Puts back an item deleted by swipe, keeping its original id. */
    public void restore(PantryItem item) {
        repository.restore(item);
    }
}
