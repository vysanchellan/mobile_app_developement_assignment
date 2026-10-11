# Screenshot checklist

Capture each shot on the emulator (Pixel, API 33 or 34) with the camera button in the emulator toolbar, or run `adb exec-out screencap -p > shotN.png`. Start from a fresh install (Device Manager > Wipe Data) so the empty states appear. Use real screenshots only.

| # | What to capture | How to get there | Caption for the report |
|---|---|---|---|
| 1 | Empty pantry | Fresh install, Pantry tab | Figure 1: Empty pantry on first launch, with a friendly prompt to add the first ingredient. |
| 2 | Empty recipes state | Fresh install, Recipes tab | Figure 2: Recipes tab with an empty pantry shows a message instead of a blank screen. |
| 3 | Add form (empty) | Tap **Add ingredient** | Figure 3: Add ingredient form with name autocomplete, quantity, unit dropdown and optional expiry date. |
| 4 | Error: empty fields | Tap **Save** with nothing filled in | Figure 4: Validation errors when Save is tapped with empty fields. |
| 5 | Error: quantity 0 | Type `0` in Quantity, tap **Save** | Figure 5: Validation error for a quantity of 0. |
| 6 | Error: bad expiry | Pick yesterday's date, fill the other fields, tap **Save** | Figure 6: Validation error for an expiry date in the past. |
| 7 | Filled form | Eggs, 6, pcs (before saving) | Figure 7: Completed form ready to save. |
| 8 | Pantry list with chips | Add Eggs 6 pcs, Tomatoes 2 pcs (expiry tomorrow), Cheddar Cheese 0.1 kg, Butter 50 g | Figure 8: Pantry list with expiry chips and the summary banner. |
| 9 | Edit screen | Tap the Eggs card | Figure 9: Edit ingredient screen opened from a pantry card through an explicit Intent. |
| 10 | Delete with Undo | Swipe a card to the left and capture while the Snackbar shows | Figure 10: Swipe-to-delete with the Undo Snackbar. |
| 11 | Delete confirm dialog | Edit screen > **Delete ingredient** | Figure 11: Delete confirmation dialog on the edit screen. |
| 12 | Ready tab with omelette | With the four demo items, open Recipes | Figure 12: Ready to cook tab listing only the Cheesy Tomato Omelette. |
| 13 | Almost there tab | Remove Butter (or tap the Almost there tab) | Figure 13: Almost there tab naming the missing ingredient. |
| 14 | Recipe detail | Tap the omelette | Figure 14: Recipe detail with ingredient check marks and numbered method. |
| 15 | Settings | Settings tab | Figure 15: Settings screen. |
| 16 | Dark mode pantry | Settings > Theme > Dark, then Pantry | Figure 16: Pantry in dark mode. |
| 17 | Rotated pantry | Rotate the emulator (Ctrl+Left) on the Pantry tab | Figure 17: Pantry in landscape orientation. |

Tip: switch the theme back to **System** after capturing the dark shots.
