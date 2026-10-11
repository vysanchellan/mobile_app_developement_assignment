package com.vysan.smartpantry.ui.pantry;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
import com.vysan.smartpantry.R;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.logic.ExpiryHelper;
import com.vysan.smartpantry.ui.settings.Prefs;

import java.time.LocalDate;
import java.util.List;

/**
 * Home tab: shows every pantry item as a card, a summary banner and an empty state.
 * Observes LiveData from PantryViewModel, so the list refreshes itself after any change.
 */
public class PantryFragment extends Fragment {

    /** Opens Add/Edit and receives its result; must be registered before the fragment starts. */
    private final ActivityResultLauncher<Intent> editorLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onEditorResult);

    private PantryViewModel viewModel;
    private PantryAdapter adapter;
    private Prefs prefs;
    private View fab;
    private RecyclerView list;
    private View emptyState;
    private MaterialCardView banner;
    private TextView bannerText;
    private ImageView bannerIcon;
    private TextView emptyTitle;
    private TextView emptyBody;
    private View emptyButton;

    public PantryFragment() {
        super(R.layout.fragment_pantry);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new Prefs(requireContext());
        list = view.findViewById(R.id.pantry_list);
        emptyState = view.findViewById(R.id.empty_state);
        banner = view.findViewById(R.id.summary_banner);
        bannerText = view.findViewById(R.id.summary_text);
        bannerIcon = view.findViewById(R.id.summary_icon);
        emptyTitle = view.findViewById(R.id.empty_title);
        emptyBody = view.findViewById(R.id.empty_body);
        emptyButton = view.findViewById(R.id.empty_add_button);

        adapter = new PantryAdapter(prefs.getEffectiveSoonWindow());
        adapter.setOnItemClickListener(item -> openEditor(item.getId()));
        list.setAdapter(adapter);
        fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> openEditor(PantryItem.NO_ID));
        emptyButton.setOnClickListener(v -> openEditor(PantryItem.NO_ID));

        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> renderState());
        viewModel.getVisibleItems().observe(getViewLifecycleOwner(), items -> renderState());
        setupSearch(view.findViewById(R.id.toolbar));
        new ItemTouchHelper(new SwipeToDeleteCallback(requireContext(),
                position -> deleteWithUndo(adapter.getItemAt(position)))).attachToRecyclerView(list);
    }

    // Delete straight away; Undo re-inserts the same row with its original id.
    private void deleteWithUndo(PantryItem item) {
        viewModel.delete(item);
        Snackbar.make(requireView(), getString(R.string.snackbar_deleted_named, item.getName()), Snackbar.LENGTH_LONG)
                .setAnchorView(fab)
                .setAction(R.string.action_undo, v -> viewModel.restore(item))
                .show();
    }

    // Explicit Intent; the item id travels as an extra so the editor knows which row to load.
    private void openEditor(long itemId) {
        Intent intent = new Intent(requireContext(), AddEditItemActivity.class);
        if (itemId != PantryItem.NO_ID) intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, itemId);
        editorLauncher.launch(intent);
    }

    private void onEditorResult(ActivityResult result) {
        Intent data = result.getData();
        if (result.getResultCode() != Activity.RESULT_OK || data == null) return;
        String outcome = data.getStringExtra(AddEditItemActivity.EXTRA_RESULT);
        int message = AddEditItemActivity.RESULT_DELETED.equals(outcome) ? R.string.snackbar_deleted
                : AddEditItemActivity.RESULT_UPDATED.equals(outcome) ? R.string.snackbar_updated
                : R.string.snackbar_added;
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).setAnchorView(fab).show();
    }

    // The search lives in the toolbar; the query is kept in the ViewModel so rotation keeps it.
    private void setupSearch(MaterialToolbar toolbar) {
        toolbar.inflateMenu(R.menu.menu_pantry);
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        String current = viewModel.getQuery();
        if (!current.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(current, false);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setQuery(newText);
                return true;
            }
        });
    }

    // Three states: empty pantry, search with no results, or the list itself.
    private void renderState() {
        List<PantryItem> all = viewModel.getItems().getValue();
        List<PantryItem> visible = viewModel.getVisibleItems().getValue();
        if (all == null || visible == null) return;
        adapter.submitList(visible);
        boolean pantryEmpty = all.isEmpty();
        boolean showEmpty = visible.isEmpty();
        list.setVisibility(showEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
        emptyTitle.setText(pantryEmpty ? getString(R.string.pantry_empty_title)
                : getString(R.string.search_empty_title, viewModel.getQuery()));
        emptyBody.setText(pantryEmpty ? R.string.pantry_empty_body : R.string.search_empty_body);
        emptyButton.setVisibility(pantryEmpty ? View.VISIBLE : View.GONE);
        banner.setVisibility(pantryEmpty ? View.GONE : View.VISIBLE);
        renderBanner(all);
    }

    // "N items · M expiring soon"; highlighted in the tomato accent only when alerts are on.
    private void renderBanner(List<PantryItem> items) {
        Resources res = getResources();
        LocalDate today = LocalDate.now();
        int soon = 0;
        int expired = 0;
        for (PantryItem item : items) {
            ExpiryHelper.Status status = ExpiryHelper.statusOf(item.getExpiryDate(), today, prefs.getSoonWindowDays());
            if (status == ExpiryHelper.Status.SOON) soon++;
            if (status == ExpiryHelper.Status.EXPIRED) expired++;
        }
        StringBuilder summary = new StringBuilder(res.getQuantityString(R.plurals.summary_items, items.size(), items.size()));
        boolean alerts = prefs.isExpiryAlertsEnabled();
        if (alerts) {
            summary.append(getString(R.string.summary_separator))
                    .append(res.getQuantityString(R.plurals.summary_expiring, soon, soon));
            if (expired > 0) {
                summary.append(getString(R.string.summary_separator))
                        .append(res.getQuantityString(R.plurals.summary_expired, expired, expired));
            }
        }
        bannerText.setText(summary);

        boolean highlight = alerts && (soon + expired) > 0;
        int background = MaterialColors.getColor(banner, highlight
                ? com.google.android.material.R.attr.colorTertiaryContainer
                : com.google.android.material.R.attr.colorSecondaryContainer);
        int foreground = MaterialColors.getColor(banner, highlight
                ? com.google.android.material.R.attr.colorOnTertiaryContainer
                : com.google.android.material.R.attr.colorOnSecondaryContainer);
        banner.setCardBackgroundColor(background);
        bannerText.setTextColor(foreground);
        bannerIcon.setImageResource(highlight ? R.drawable.ic_warning : R.drawable.ic_info);
        bannerIcon.setImageTintList(ColorStateList.valueOf(foreground));
    }
}
