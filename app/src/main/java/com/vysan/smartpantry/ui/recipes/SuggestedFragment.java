package com.vysan.smartpantry.ui.recipes;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.vysan.smartpantry.R;
import com.vysan.smartpantry.data.model.AlmostThere;
import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.logic.IngredientNormalizer;
import com.vysan.smartpantry.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipes tab with two lists: "Ready to cook" (strict matches only) and "Almost there"
 * (exactly one ingredient short). The two lists come from separate matcher methods.
 */
public class SuggestedFragment extends Fragment {

    private RecipesViewModel viewModel;
    private RecipeAdapter adapter;
    private TabLayout tabs;
    private RecyclerView list;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;

    public SuggestedFragment() {
        super(R.layout.fragment_recipes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RecipesViewModel.class);
        list = view.findViewById(R.id.recipe_list);
        adapter = new RecipeAdapter(recipe -> RecipeDetailActivity.start(requireContext(), recipe.getId()));
        list.setAdapter(adapter);
        emptyState = view.findViewById(R.id.recipes_empty);
        emptyTitle = view.findViewById(R.id.recipes_empty_title);
        emptyBody = view.findViewById(R.id.recipes_empty_body);
        view.findViewById(R.id.recipes_empty_button).setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) ((MainActivity) requireActivity()).selectTab(R.id.nav_pantry);
        });

        tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText(R.string.tab_ready));
        tabs.addTab(tabs.newTab().setText(R.string.tab_almost));
        TabLayout.Tab selected = tabs.getTabAt(viewModel.getSelectedTab());
        if (selected != null) selected.select();
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setSelectedTab(tab.getPosition());
                render(viewModel.getResult().getValue());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                list.smoothScrollToPosition(0);
            }
        });

        viewModel.getResult().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(RecipesViewModel.MatchResult result) {
        if (result == null) return;
        setTabCount(RecipesViewModel.TAB_READY, R.string.tab_ready_count, result.ready.size());
        setTabCount(RecipesViewModel.TAB_ALMOST, R.string.tab_almost_count, result.almost.size());

        boolean readyTab = viewModel.getSelectedTab() == RecipesViewModel.TAB_READY;
        List<RecipeAdapter.Row> rows = readyTab ? readyRows(result.ready) : almostRows(result);
        adapter.submitList(rows);

        // Never a blank screen: an empty tab explains why and offers a way forward.
        boolean empty = rows.isEmpty();
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        emptyTitle.setText(readyTab ? R.string.recipes_empty_ready_title : R.string.recipes_empty_almost_title);
        emptyBody.setText(readyTab ? R.string.recipes_empty_ready_body : R.string.recipes_empty_almost_body);
    }

    private void setTabCount(int index, int format, int count) {
        TabLayout.Tab tab = tabs.getTabAt(index);
        if (tab != null) tab.setText(getString(format, count));
    }

    private static List<RecipeAdapter.Row> readyRows(List<Recipe> recipes) {
        List<RecipeAdapter.Row> rows = new ArrayList<>();
        for (Recipe recipe : recipes) rows.add(RecipeAdapter.Row.ready(recipe));
        return rows;
    }

    private static List<RecipeAdapter.Row> almostRows(RecipesViewModel.MatchResult result) {
        List<RecipeAdapter.Row> rows = new ArrayList<>();
        for (AlmostThere almost : result.almost) {
            boolean partlyStocked = result.stockedNames.contains(
                    IngredientNormalizer.normalize(almost.getMissing().getName()));
            rows.add(RecipeAdapter.Row.almost(almost.getRecipe(), almost.getMissing(), partlyStocked));
        }
        return rows;
    }
}
