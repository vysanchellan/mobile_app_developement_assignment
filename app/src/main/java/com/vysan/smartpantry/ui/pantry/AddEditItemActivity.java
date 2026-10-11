package com.vysan.smartpantry.ui.pantry;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.vysan.smartpantry.R;
import com.vysan.smartpantry.data.db.SeedData;
import com.vysan.smartpantry.data.model.PantryItem;
import com.vysan.smartpantry.data.repo.PantryRepository;
import com.vysan.smartpantry.logic.ItemValidator;
import com.vysan.smartpantry.ui.common.Format;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Form to add a new ingredient or edit an existing one. Opened with an explicit Intent; when
 * EXTRA_ITEM_ID is present the screen is in edit mode. Returns the outcome via setResult.
 */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.vysan.smartpantry.EXTRA_ITEM_ID";
    public static final String EXTRA_RESULT = "com.vysan.smartpantry.EXTRA_RESULT";
    public static final String RESULT_ADDED = "added";
    public static final String RESULT_UPDATED = "updated";
    public static final String RESULT_DELETED = "deleted";

    /** The units offered in the dropdown, in display order. */
    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};
    private static final String TAG_DATE_PICKER = "expiry_picker";
    private static final String STATE_EXPIRY = "expiry";
    private static final String STATE_INITIAL = "initial";
    private static final String STATE_CREATED_AT = "created_at";

    private PantryRepository repository;
    private long itemId;
    private boolean isEdit;
    private long createdAt;
    private String expiryIso;
    private String initialSnapshot = "";
    private boolean saving;

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout unitLayout;
    private TextInputLayout expiryLayout;
    private MaterialAutoCompleteTextView nameInput;
    private TextInputEditText quantityInput;
    private MaterialAutoCompleteTextView unitInput;
    private TextInputEditText expiryInput;
    private MaterialButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        repository = PantryRepository.getInstance(this);
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, PantryItem.NO_ID);
        isEdit = itemId != PantryItem.NO_ID;
        bindViews();
        setupToolbar();
        setupFields();
        setupBackHandling();

        if (savedInstanceState != null) {
            expiryIso = savedInstanceState.getString(STATE_EXPIRY);
            initialSnapshot = savedInstanceState.getString(STATE_INITIAL, "");
            createdAt = savedInstanceState.getLong(STATE_CREATED_AT);
            renderExpiry();
        } else if (isEdit) {
            saveButton.setEnabled(false);   // re-enabled once the item has loaded
            repository.loadItem(itemId, this::populate);
        } else {
            renderExpiry();
            initialSnapshot = snapshot();
        }
    }

    // Set here, not in onCreate: restoring the field text would otherwise filter the list to one unit.
    @Override
    protected void onResume() {
        super.onResume();
        unitInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, UNITS));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, expiryIso);
        outState.putString(STATE_INITIAL, initialSnapshot);
        outState.putLong(STATE_CREATED_AT, createdAt);
    }

    private void bindViews() {
        nameLayout = findViewById(R.id.name_layout);
        quantityLayout = findViewById(R.id.quantity_layout);
        unitLayout = findViewById(R.id.unit_layout);
        expiryLayout = findViewById(R.id.expiry_layout);
        nameInput = findViewById(R.id.name_input);
        quantityInput = findViewById(R.id.quantity_input);
        unitInput = findViewById(R.id.unit_input);
        expiryInput = findViewById(R.id.expiry_input);
        saveButton = findViewById(R.id.save_button);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(isEdit ? R.string.title_edit_item : R.string.title_add_item);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupFields() {
        nameInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                SeedData.INGREDIENT_VOCABULARY));
        clearErrorOnEdit(nameLayout, nameInput);
        clearErrorOnEdit(quantityLayout, quantityInput);
        unitInput.setOnItemClickListener((parent, view, position, id) -> unitLayout.setError(null));
        // The unit is picked from a list, so the keyboard would only hide the options.
        unitInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) hideKeyboard(v);
        });

        // Done on the quantity keyboard closes it and opens the unit list if no unit is chosen yet.
        quantityInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) return false;
            hideKeyboard(v);
            v.clearFocus();
            if (text(unitInput).isEmpty()) unitInput.showDropDown();
            return true;
        });

        expiryInput.setOnClickListener(v -> showDatePicker());
        expiryLayout.setStartIconOnClickListener(v -> showDatePicker());
        expiryLayout.setEndIconOnClickListener(v -> {
            expiryIso = null;
            renderExpiry();
        });
        reattachDatePicker();

        saveButton.setOnClickListener(v -> attemptSave());
        MaterialButton deleteButton = findViewById(R.id.delete_button);
        deleteButton.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!saving && !snapshot().equals(initialSnapshot)) {
                    confirmDiscard();
                } else {
                    finish();
                }
            }
        });
    }

    private void populate(PantryItem item) {
        if (item == null) {   // deleted elsewhere while this screen was opening
            Toast.makeText(this, R.string.item_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nameInput.setText(item.getName(), false);
        quantityInput.setText(Format.quantity(item.getQuantity()));
        unitInput.setText(item.getUnit(), false);
        expiryIso = item.getExpiryDate();
        createdAt = item.getCreatedAt();
        renderExpiry();
        initialSnapshot = snapshot();
        saveButton.setEnabled(true);
    }

    // Validates every field at once so the user sees all problems together.
    private void attemptSave() {
        if (saving) return;
        String name = ItemValidator.cleanName(text(nameInput));
        String quantity = text(quantityInput);
        String unit = text(unitInput);
        boolean valid = showError(nameLayout, nameError(ItemValidator.validateName(name)));
        valid &= showError(quantityLayout, quantityError(ItemValidator.validateQuantity(quantity)));
        valid &= showError(unitLayout, ItemValidator.validateUnit(unit) == ItemValidator.Result.OK
                ? 0 : R.string.error_unit_required);
        valid &= showError(expiryLayout, ItemValidator.validateExpiry(expiryIso, LocalDate.now(), !isEdit)
                == ItemValidator.Result.OK ? 0 : R.string.error_expiry_past);
        if (!valid) return;

        saving = true;   // blocks double taps from saving twice
        saveButton.setEnabled(false);
        long created = isEdit ? createdAt : System.currentTimeMillis();
        PantryItem item = new PantryItem(itemId, name, ItemValidator.parseQuantity(quantity), unit, expiryIso, created);
        if (isEdit) {
            repository.update(item, rows -> finishWith(RESULT_UPDATED));
        } else {
            repository.insert(item, id -> finishWith(RESULT_ADDED));
        }
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, text(nameInput)))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    saving = true;
                    repository.delete(itemId);
                    finishWith(RESULT_DELETED);
                })
                .show();
    }

    private void confirmDiscard() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_discard_title)
                .setMessage(R.string.dialog_discard_message)
                .setNegativeButton(R.string.action_keep_editing, null)
                .setPositiveButton(R.string.action_discard, (dialog, which) -> finish())
                .show();
    }

    private void showDatePicker() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_DATE_PICKER) != null) return;   // already open
        long selection = expiryIso != null
                ? LocalDate.parse(expiryIso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                : MaterialDatePicker.todayInUtcMilliseconds();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.field_expiry_pick)
                .setSelection(selection)
                .build();
        listenToPicker(picker);
        picker.show(getSupportFragmentManager(), TAG_DATE_PICKER);
    }

    // After rotation the picker is restored by the FragmentManager but its listener is not.
    @SuppressWarnings("unchecked")
    private void reattachDatePicker() {
        Object existing = getSupportFragmentManager().findFragmentByTag(TAG_DATE_PICKER);
        if (existing instanceof MaterialDatePicker) listenToPicker((MaterialDatePicker<Long>) existing);
    }

    // The picker reports midnight UTC, so convert in UTC to avoid an off-by-one day.
    private void listenToPicker(MaterialDatePicker<Long> picker) {
        picker.addOnPositiveButtonClickListener(millis -> {
            expiryIso = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString();
            expiryLayout.setError(null);
            renderExpiry();
        });
    }

    private void renderExpiry() {
        expiryInput.setText(expiryIso == null ? "" : Format.displayDate(expiryIso));
        expiryLayout.setEndIconVisible(expiryIso != null);
    }

    private void finishWith(String result) {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT, result));
        finish();
    }

    /** Compact copy of the form used to detect unsaved changes. */
    private String snapshot() {
        return text(nameInput) + '|' + text(quantityInput) + '|' + text(unitInput) + '|' + expiryIso;
    }

    private static String text(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private static boolean showError(TextInputLayout layout, @StringRes int message) {
        layout.setError(message == 0 ? null : layout.getContext().getString(message));
        return message == 0;
    }

    @StringRes
    private static int nameError(ItemValidator.Result result) {
        switch (result) {
            case EMPTY: return R.string.error_name_required;
            case TOO_LONG: return R.string.error_name_too_long;
            case INVALID: return R.string.error_name_invalid;
            default: return 0;
        }
    }

    @StringRes
    private static int quantityError(ItemValidator.Result result) {
        switch (result) {
            case EMPTY: return R.string.error_quantity_required;
            case NOT_A_NUMBER: return R.string.error_quantity_number;
            case NOT_POSITIVE: return R.string.error_quantity_positive;
            case TOO_LARGE: return R.string.error_quantity_too_large;
            default: return 0;
        }
    }

    // Errors disappear as soon as the user starts fixing the field.
    private static void clearErrorOnEdit(TextInputLayout layout, EditText input) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
