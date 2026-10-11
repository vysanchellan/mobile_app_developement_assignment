package com.vysan.smartpantry.ui.pantry;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
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

    private PantryAdapter adapter;
    private Prefs prefs;
    private RecyclerView list;
    private View emptyState;
    private MaterialCardView banner;
    private TextView bannerText;
    private ImageView bannerIcon;

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

        adapter = new PantryAdapter(prefs.getEffectiveSoonWindow());
        list.setAdapter(adapter);

        PantryViewModel viewModel = new ViewModelProvider(this).get(PantryViewModel.class);
        viewModel.getItems().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(List<PantryItem> items) {
        adapter.submitList(items);
        boolean empty = items.isEmpty();
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        banner.setVisibility(empty ? View.GONE : View.VISIBLE);
        renderBanner(items);
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
