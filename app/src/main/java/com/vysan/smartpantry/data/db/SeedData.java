package com.vysan.smartpantry.data.db;

import com.vysan.smartpantry.data.model.Recipe;
import com.vysan.smartpantry.data.model.RecipeIngredient;

import java.util.Arrays;
import java.util.List;

/**
 * The 20 recipes inserted on first run, plus the shared ingredient vocabulary used by the
 * name autocomplete. Pure Java so the demo scenario can be verified by a unit test.
 * Recipes deliberately overlap on everyday ingredients so the suggestions change as the pantry grows.
 */
public final class SeedData {

    private static final String G = "g";
    private static final String KG = "kg";
    private static final String ML = "ml";
    private static final String TSP = "tsp";
    private static final String TBSP = "tbsp";
    private static final String CUP = "cup";
    private static final String PCS = "pcs";

    /** Suggestions shown while typing an ingredient name. Free text is still allowed. */
    public static final String[] INGREDIENT_VOCABULARY = {
            "Apples", "Avocado", "Baked beans", "Bananas", "Beef mince", "Bell pepper", "Bread",
            "Butter", "Butternut", "Cabbage", "Carrots", "Cheddar cheese", "Chicken breast",
            "Chickpeas", "Cinnamon", "Cream", "Cucumber", "Curry powder", "Eggs", "Feta cheese",
            "Flour", "Garlic", "Green beans", "Honey", "Lemon juice", "Lentils", "Maize meal",
            "Mayonnaise", "Milk", "Mushrooms", "Oats", "Olive oil", "Olives", "Onions", "Pasta",
            "Peanut butter", "Potatoes", "Rice", "Salt", "Soy sauce", "Spinach", "Sugar",
            "Sunflower oil", "Sweetcorn", "Tomatoes", "Tuna", "Vegetable stock", "Yoghurt"
    };

    private SeedData() {
    }

    /** Builds the seed recipes. Ids are 0 because the database assigns them on insert. */
    public static List<Recipe> recipes() {
        return Arrays.asList(
                recipe("Cheesy Tomato Omelette", "A fluffy three-egg omelette with melted cheddar and fresh tomato.", 10, 1,
                        steps("Whisk the eggs in a bowl.", "Dice the tomato and grate the cheddar.",
                                "Melt the butter in a pan over medium heat.", "Pour in the eggs and cook until almost set.",
                                "Scatter tomato and cheddar over one half, fold and serve."),
                        ing("Eggs", 3, PCS), ing("Tomato", 1, PCS), ing("Cheddar cheese", 30, G), ing("Butter", 10, G)),
                recipe("Garlic Butter Pasta", "Simple pasta tossed in golden garlic butter.", 20, 2,
                        steps("Boil the pasta in salted water until al dente.", "Finely chop the garlic.",
                                "Warm the olive oil and butter in a pan.", "Fry the garlic gently for one minute.",
                                "Toss the drained pasta in the garlic butter and serve."),
                        ing("Pasta", 200, G), ing("Butter", 30, G), ing("Garlic", 2, PCS), ing("Olive oil", 1, TBSP)),
                recipe("Egg Fried Rice", "A quick way to use up leftover rice.", 25, 2,
                        steps("Cook the rice and let it cool.", "Chop the onion finely.",
                                "Fry the onion until soft.", "Push the onion aside and scramble the eggs.",
                                "Add the rice and soy sauce and stir-fry for three minutes."),
                        ing("Rice", 200, G), ing("Eggs", 2, PCS), ing("Soy sauce", 2, TBSP), ing("Onion", 1, PCS)),
                recipe("Scrambled Eggs on Toast", "Creamy scrambled eggs on buttered toast.", 10, 1,
                        steps("Toast the bread.", "Whisk the eggs with the milk.",
                                "Melt the butter in a pan over low heat.", "Stir the eggs slowly until just set.",
                                "Spoon onto the toast and serve."),
                        ing("Eggs", 3, PCS), ing("Butter", 10, G), ing("Milk", 50, ML), ing("Bread", 2, PCS)),
                recipe("French Toast", "Sweet cinnamon eggy bread.", 15, 2,
                        steps("Whisk the eggs, milk, sugar and cinnamon.", "Soak each slice of bread for a few seconds.",
                                "Heat a non-stick pan.", "Fry the bread until golden on both sides.", "Serve warm."),
                        ing("Bread", 4, PCS), ing("Eggs", 2, PCS), ing("Milk", 100, ML), ing("Sugar", 1, TBSP),
                        ing("Cinnamon", 1, TSP)),
                recipe("Tomato and Onion Smoor", "A classic South African tomato and onion relish.", 25, 4,
                        steps("Slice the onions and chop the tomatoes.", "Heat the sunflower oil in a pot.",
                                "Fry the onions until soft and golden.", "Add the tomatoes, sugar and salt.",
                                "Simmer for fifteen minutes until thick."),
                        ing("Tomatoes", 3, PCS), ing("Onions", 2, PCS), ing("Sunflower oil", 2, TBSP),
                        ing("Sugar", 1, TSP), ing("Salt", 1, TSP)),
                recipe("Chakalaka", "A spicy vegetable relish to serve with pap or braai.", 30, 4,
                        steps("Grate the carrots and dice the onion and pepper.", "Fry the onion in the oil until soft.",
                                "Add the curry powder and cook for one minute.", "Add the carrots and pepper and cook for five minutes.",
                                "Stir in the baked beans and simmer for ten minutes."),
                        ing("Baked beans", 400, G), ing("Carrots", 2, PCS), ing("Onion", 1, PCS),
                        ing("Bell pepper", 1, PCS), ing("Curry powder", 1, TBSP), ing("Sunflower oil", 2, TBSP)),
                recipe("Cheese and Tomato Toastie", "A golden toasted sandwich with cheddar and tomato.", 10, 1,
                        steps("Butter the outside of both slices of bread.", "Slice the tomato and cheddar.",
                                "Layer the filling between the unbuttered sides.", "Toast in a pan until golden on both sides.",
                                "Cut in half and serve hot."),
                        ing("Bread", 2, PCS), ing("Cheddar cheese", 40, G), ing("Tomato", 1, PCS), ing("Butter", 10, G)),
                recipe("Flapjacks", "Small, thick South African pancakes.", 20, 4,
                        steps("Mix the flour and sugar in a bowl.", "Whisk in the egg and milk to a thick batter.",
                                "Melt a little butter in a pan.", "Drop spoonfuls of batter and cook until bubbles appear.",
                                "Flip and cook until golden."),
                        ing("Flour", 1, CUP), ing("Milk", 1, CUP), ing("Eggs", 1, PCS), ing("Sugar", 2, TBSP),
                        ing("Butter", 20, G)),
                recipe("Spaghetti Bolognaise", "A hearty beef mince and tomato pasta sauce.", 40, 4,
                        steps("Chop the onion, garlic and tomatoes.", "Brown the mince in the olive oil.",
                                "Add the onion and garlic and cook until soft.", "Add the tomatoes and simmer for twenty minutes.",
                                "Cook the pasta and serve with the sauce."),
                        ing("Beef mince", 500, G), ing("Pasta", 400, G), ing("Tomatoes", 4, PCS), ing("Onion", 1, PCS),
                        ing("Garlic", 2, PCS), ing("Olive oil", 1, TBSP)),
                recipe("Chicken Stir-Fry", "Tender chicken and peppers in soy sauce over rice.", 30, 4,
                        steps("Cook the rice.", "Slice the chicken and pepper into strips.", "Chop the garlic.",
                                "Stir-fry the chicken in the oil until cooked through.",
                                "Add the pepper, garlic and soy sauce, then serve over rice."),
                        ing("Chicken breast", 400, G), ing("Bell pepper", 1, PCS), ing("Soy sauce", 3, TBSP),
                        ing("Garlic", 2, PCS), ing("Rice", 300, G), ing("Sunflower oil", 2, TBSP)),
                recipe("Potato and Onion Hash", "Crispy potatoes and onion topped with fried eggs.", 25, 2,
                        steps("Dice the potatoes and boil for five minutes.", "Slice the onion.",
                                "Fry the potatoes and onion in the butter until crisp.", "Make two wells and crack in the eggs.",
                                "Cover and cook until the eggs are set."),
                        ing("Potatoes", 4, PCS), ing("Onion", 1, PCS), ing("Butter", 20, G), ing("Eggs", 2, PCS)),
                recipe("Butternut Soup", "Smooth, creamy butternut soup.", 45, 4,
                        steps("Peel and cube the butternut.", "Soften the chopped onion in the butter.",
                                "Add the butternut and stock and simmer for twenty-five minutes.", "Blend until smooth.",
                                "Stir in the cream and serve."),
                        ing("Butternut", 1, KG), ing("Onion", 1, PCS), ing("Vegetable stock", 750, ML),
                        ing("Cream", 100, ML), ing("Butter", 20, G)),
                recipe("Tuna Pasta Salad", "A cold pasta salad for lunchboxes.", 20, 3,
                        steps("Cook the pasta and rinse under cold water.", "Drain the tuna and sweetcorn.",
                                "Dice the cucumber.", "Mix everything with the mayonnaise.", "Chill before serving."),
                        ing("Pasta", 250, G), ing("Tuna", 170, G), ing("Mayonnaise", 3, TBSP), ing("Sweetcorn", 200, G),
                        ing("Cucumber", 1, PCS)),
                recipe("Greek Salad", "A fresh salad with feta and olives.", 10, 2,
                        steps("Chop the cucumber and tomatoes into chunks.", "Slice the onion thinly.",
                                "Combine the vegetables with the olives.", "Crumble the feta over the top.",
                                "Drizzle with olive oil and serve."),
                        ing("Cucumber", 1, PCS), ing("Tomatoes", 2, PCS), ing("Feta cheese", 100, G), ing("Onion", 1, PCS),
                        ing("Olive oil", 2, TBSP), ing("Olives", 50, G)),
                recipe("Banana Smoothie", "A creamy breakfast smoothie.", 5, 2,
                        steps("Peel and slice the bananas.", "Add the bananas, milk, yoghurt and honey to a blender.",
                                "Blend until smooth.", "Pour into glasses and serve."),
                        ing("Bananas", 2, PCS), ing("Milk", 250, ML), ing("Yoghurt", 125, G), ing("Honey", 1, TBSP)),
                recipe("Honey Banana Oats", "Warm oat porridge topped with banana and honey.", 10, 1,
                        steps("Add the oats and milk to a small pot.", "Simmer, stirring, for five minutes.",
                                "Slice the banana.", "Top the porridge with banana and a drizzle of honey."),
                        ing("Oats", 80, G), ing("Milk", 250, ML), ing("Honey", 1, TBSP), ing("Banana", 1, PCS)),
                recipe("Lentil Curry", "A warming, budget-friendly red lentil curry.", 35, 4,
                        steps("Rinse the lentils.", "Fry the chopped onion and garlic in the oil.",
                                "Stir in the curry powder for one minute.", "Add the chopped tomatoes and lentils with a little water.",
                                "Simmer for twenty minutes until the lentils are soft."),
                        ing("Lentils", 250, G), ing("Onion", 1, PCS), ing("Garlic", 2, PCS), ing("Curry powder", 2, TBSP),
                        ing("Tomatoes", 2, PCS), ing("Sunflower oil", 1, TBSP)),
                recipe("Creamy Chicken and Mushroom Pasta", "Chicken and mushrooms in a garlic cream sauce.", 30, 3,
                        steps("Cook the pasta.", "Slice the chicken and mushrooms.", "Fry the chicken until golden.",
                                "Add the mushrooms and garlic and cook for five minutes.",
                                "Pour in the cream, simmer briefly and toss with the pasta."),
                        ing("Chicken breast", 300, G), ing("Mushrooms", 250, G), ing("Cream", 250, ML), ing("Pasta", 300, G),
                        ing("Garlic", 2, PCS)),
                recipe("Avocado and Egg Toast", "Smashed avocado on toast with a fried egg.", 10, 2,
                        steps("Toast the bread.", "Mash the avocado with the lemon juice.", "Fry the eggs.",
                                "Spread the avocado on the toast.", "Top each slice with an egg."),
                        ing("Bread", 2, PCS), ing("Avocado", 1, PCS), ing("Eggs", 2, PCS), ing("Lemon juice", 1, TSP))
        );
    }

    private static Recipe recipe(String name, String description, int minutes, int servings,
                                 List<String> steps, RecipeIngredient... ingredients) {
        return new Recipe(0, name, description, minutes, servings, steps, Arrays.asList(ingredients));
    }

    private static List<String> steps(String... steps) {
        return Arrays.asList(steps);
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}
