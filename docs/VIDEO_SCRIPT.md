# Video script: Smart Pantry Manager (target 6:40, hard limit 7:00)

Record at 1080p, MP4 (H.264), with narration throughout. Speak at about 130 words per minute and pause briefly while tapping. Line numbers below refer to the files as committed. If you edit these files, re-check them with Ctrl+G in Android Studio.

## Pre-recording setup (once)

1. **Fresh install.** In Android Studio, open Device Manager, choose **Wipe Data** on a Pixel emulator (API 33 or 34), then **Run** the app so the database starts empty.
2. **Windows.** Put Android Studio on the left and open these four tabs: `DatabaseHelper.java`, `PantryDao.java`, `PantryAdapter.java` and `RecipeMatcher.java` (also `PantryFragment.java`, `IngredientNormalizer.java` and `UnitConverter.java`). Put the emulator on the right. Open a browser tab at <https://github.com/vysanchellan/mobile_app_developement_assignment/commits/main>.
3. **Database Inspector.** Open View > Tool Windows > App Inspection > Database Inspector, select the `com.vysan.smartpantry` process and `smart_pantry.db`, and dock it at the bottom, closed for now.
4. Set the editor font to 16-18 pt, turn on Do Not Disturb, and make a 20-second test recording.
5. In OBS, record at 1080p and 30 fps with your microphone selected. Do one timed dry run first.
6. Before recording, check the commit count on GitHub (or run `git log --oneline | wc -l`) and put it in place of **[N]** below.

---

## Segment A: GitHub walkthrough (0:00-0:45, about 95 words)

**Show:** the commits page. Scroll slowly from the oldest commit to the newest, then click the README.

**Say:** "Hi, I'm [Name], student number [number], and this is my Smart Pantry Manager for Mobile App Development 700. It's a Java Android app that tracks what's in your pantry and suggests only the recipes you can cook right now with exactly what you have, to cut food waste. This is my GitHub repository. I built it incrementally: first the project setup and database, then the matching logic and its unit tests, then each screen, and finally settings, polish and the README, with [N] commits in total. The README explains the setup and why I chose SQLite."

## Segment B: Live app demo (0:45-3:30)

| Time | Do this | Say |
|---|---|---|
| 0:45 | Show the fresh Pantry screen with its empty state. | "A fresh install, so the pantry is empty." |
| 0:55 | Tap the **Recipes** tab. The message "No recipes match your pantry yet" is visible. | "With nothing in the pantry, the app shows a friendly message instead of a blank screen." |
| 1:05 | Go back to Pantry and tap **Add ingredient**. Tap **Save** with everything empty to show the errors, then type `0` in Quantity and tap Save again. | "Input validation stops empty names and zero quantities." |
| 1:20 | **Create:** add `Eggs`, 6, pcs and save. Add `Tomatoes`, 2, pcs, with expiry set to tomorrow in the calendar. Add `Cheddar Cheese`, 0.1, kg. | "That's Create. Notice the expiry chip warning on the tomatoes, and that I entered cheddar in kilograms and tomatoes in the plural, which the matcher handles." |
| 1:50 | **Read:** open the Recipes tab. Ready to cook (0) is empty. Tap **Almost there (1)**: Cheesy Tomato Omelette, "Missing: Butter". | "The omelette needs butter, so it is NOT suggested. It sits only in the separate Almost there list. This is the strict rule." |
| 2:10 | Add `Butter`, 50, g. Open **Ready to cook (1)** to show the omelette. Tap it to open Recipe Detail, and briefly show the ticks and steps. | "Now I have every ingredient, so it appears, and the detail screen shows the method." |
| 2:30 | **Update:** go to Pantry, tap Eggs, change the quantity to 2 and save. In Recipes, the omelette is gone ("Need more: Eggs (3 pcs)" under Almost there). Change Eggs back to 6 and it returns. | "Update works, and quantity matters: two eggs is not enough, six is." |
| 2:55 | **Delete:** swipe Butter to the left, show the Undo Snackbar, and let it time out. In Recipes, the omelette is back under Almost there. | "Delete works with undo, and the recipe disappears from the suggestions immediately." |
| 3:10 | **Persistence:** press the red Stop button in Android Studio (or swipe the app away from Recents), then relaunch. Eggs, Tomatoes and Cheddar are still there. | "After fully closing and reopening, the data is still here, so it really persists." |
| 3:25 | Open Settings: toggle expiring-soon alerts and tap **Dark**. | "There's also a settings screen." (Stop at 3:30.) |

## Segment C: Concept explanations (3:30-6:00)

### Concept 1: Database end to end (3:30-4:20, about 110 words)

**Show:**
- `DatabaseHelper.java`: the CREATE TABLE strings at **L35-L61**, `onConfigure` (foreign keys on) at **L81**, and `onCreate` at **L85-L101** (seeding loop L93-L95, `setTransactionSuccessful` L97).
- `PantryDao.java`: `insert` at **L41**, `getAll` at **L47** (try-with-resources Cursor at L50), `update` at **L66**, `delete` at **L73**, `toValues` at **L92** and `fromCursor` at **L103**.
- Database Inspector: run `SELECT * FROM pantry_items;`

**Say:** "My database is SQLite through SQLiteOpenHelper. The first time the app runs, onCreate executes these CREATE TABLE statements: pantry_items, recipes and recipe_ingredients, linked by a foreign key, and it seeds twenty recipes inside one transaction. The DAO turns Java objects into ContentValues for insert and update, uses parameterised where clauses so there is no SQL injection, and reads rows back through a Cursor into PantryItem objects. Here in Database Inspector I run a SELECT and you can see the same rows I created in the app, which proves the data lives in the database file."

### Concept 2: RecyclerView and Adapter (4:20-5:10, about 110 words)

**Show:**
- `PantryAdapter.java`: the DiffUtil callback at **L34-L44** (`areItemsTheSame` L36, `areContentsTheSame` L41), `onCreateViewHolder` at **L60**, `onBindViewHolder` at **L74-L81**, and the ViewHolder at **L120**.
- `PantryFragment.java`: the LiveData observers at **L83-L84** and `adapter.submitList` at **L148**.

**Say:** "The pantry list is a RecyclerView with my own PantryAdapter. onCreateViewHolder inflates the card layout once per visible row, and onBindViewHolder fills a row with the name, quantity and expiry chip for the item at that position, so rows are recycled and scrolling is smooth. I use DiffUtil so only changed rows animate. The fragment observes LiveData from the ViewModel, which reads from the database on a background thread, so whenever I add, edit or delete, the list updates by itself."

### Concept 3: Strict matching (5:10-6:00, about 110 words)

**Show:**
- `RecipeMatcher.java`: `findStrictMatches` at **L24-L32**, the early `continue` at **L28**, `canCook` with its early `return false` at **L51-L57**, `buildStock` (summing into a map) at **L69-L80** with the `merge` at **L77**, and the epsilon comparison at **L89**.
- `IngredientNormalizer.java`: `singularize` at **L60**, the "tomatoes to tomato" rule at **L66**.
- `UnitConverter.java`: the kg factor at **L31** and `toBase` at **L72**.

**Say:** "This is the core rule. First the pantry is normalised, so tomatoes becomes tomato, and converted to base units, so zero point one kilogram becomes a hundred grams. Duplicates are summed into a map. Then for each recipe I loop through every required ingredient, and if even one is missing or the quantity is short, I skip the recipe immediately. Only a recipe that survives all ingredients is added to the suggestions. So four out of five is never shown. I covered this with JUnit tests, including this exact case."
(Optional: flash `RecipeMatcherTest.fourOfFiveIngredientsIsExcluded` as you say the last sentence.)

## Segment D: Database justification (6:00-6:40, about 85 words)

**Show:** the "Why SQLite?" section of the README on GitHub, or `docs/schema.sql`.

**Say:** "I chose SQLite because this app is offline and personal: the pantry never needs a server, so there is no hosting cost, no account, no network dependency and no privacy risk. It is built into Android and covered in our module. SQLiteOpenHelper let me write and show the SQL explicitly, use transactions for seeding and keep CRUD fast on the device. Firebase or PostgreSQL would add a backend and internet requirement with no benefit for a single user's pantry. Thanks for watching."

---

## After recording

Compress (target 25-40 MB):

```bash
ffmpeg -i raw.mp4 -vf scale=1920:1080 -c:v libx264 -crf 28 -preset slow -c:a aac -b:a 96k final.mp4
```

Check that the video runs between 5:00 and 7:00, the voice is audible throughout, the code is readable, and the final ZIP is under 50 MB (the packaging script checks the size).
