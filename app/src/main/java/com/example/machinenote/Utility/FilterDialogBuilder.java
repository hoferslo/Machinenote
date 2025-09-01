// FilterDialogBuilder.java - Helper class for creating filter dialogs with collapsible sections
package com.example.machinenote.Utility;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.machinenote.R; // Import za R class
import java.util.ArrayList;
import java.util.List;

public class FilterDialogBuilder {

    public interface FilterDialogCallback {
        void onFilterSelected(GenericFilter.SortOrder globalSortOrder, GenericFilter.FilterCriteria[] criteria);
    }

    public static class FilterField {
        private String fieldName;
        private String displayName;
        private List<String> possibleValues;

        public FilterField(String fieldName, String displayName, List<String> possibleValues) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.possibleValues = possibleValues;
        }

        // Getters
        public String getFieldName() { return fieldName; }
        public String getDisplayName() { return displayName; }
        public List<String> getPossibleValues() { return possibleValues; }
    }

    public static void showFilterDialog(Context context, List<FilterField> fields, FilterDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Dialog_Custom);
        builder.setTitle("Filter Options");

        // Main scroll view for the dialog
        ScrollView scrollView = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.surface_primary));
        scrollView.addView(layout);

        // Global Sort Section
        TextView sortLabel = new TextView(context);
        sortLabel.setText("Sort All Items By Name:");
        sortLabel.setTextSize(16);
        sortLabel.setTypeface(null, Typeface.BOLD);
        sortLabel.setTextColor(ContextCompat.getColor(context, R.color.content_primary));
        sortLabel.setPadding(0, 0, 0, 10);
        layout.addView(sortLabel);

        Spinner globalSortSpinner = new Spinner(context);
        String[] sortOptions = {"No Sorting", "Ascending", "Descending"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, sortOptions) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(ContextCompat.getColor(context, R.color.content_primary));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(ContextCompat.getColor(context, R.color.content_primary));
                textView.setBackgroundColor(ContextCompat.getColor(context, R.color.surface_primary));
                return view;
            }
        };
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        globalSortSpinner.setAdapter(sortAdapter);
        globalSortSpinner.setBackgroundColor(ContextCompat.getColor(context, R.color.surface_tertiary));
        layout.addView(globalSortSpinner);

        // Add separator
        TextView separator = new TextView(context);
        separator.setText("━━━━━━━━━━━━━━━━━━━━━");
        separator.setPadding(0, 20, 0, 20);
        separator.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        separator.setTextColor(ContextCompat.getColor(context, R.color.border_primary));
        layout.addView(separator);

        // Store checkbox references for each field
        List<List<CheckBox>> fieldCheckBoxes = new ArrayList<>();

        // Create collapsible sections for each field
        for (int i = 0; i < fields.size(); i++) {
            FilterField field = fields.get(i);

            // Create collapsible header with arrow
            TextView fieldHeader = new TextView(context);
            fieldHeader.setText("▶ " + field.getDisplayName()); // Right arrow initially (collapsed)
            fieldHeader.setTextSize(16);
            fieldHeader.setTypeface(null, Typeface.BOLD);
            fieldHeader.setTextColor(ContextCompat.getColor(context, R.color.content_primary));
            fieldHeader.setPadding(10, 15, 10, 10);
            fieldHeader.setClickable(true);
            fieldHeader.setFocusable(true);

            // Add visual feedback for clickable header
            fieldHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.surface_secondary));

            layout.addView(fieldHeader);

            // Create container for the collapsible content
            LinearLayout collapsibleContent = new LinearLayout(context);
            collapsibleContent.setOrientation(LinearLayout.VERTICAL);
            collapsibleContent.setPadding(20, 10, 0, 10); // Indent the content
            collapsibleContent.setBackgroundColor(ContextCompat.getColor(context, R.color.background_section));
            collapsibleContent.setVisibility(View.GONE); // Start collapsed
            layout.addView(collapsibleContent);

            // Create checkboxes for each possible value
            List<CheckBox> checkBoxes = new ArrayList<>();
            if (field.getPossibleValues() != null && !field.getPossibleValues().isEmpty()) {

                // "Select All" checkbox
                CheckBox selectAllCheckBox = new CheckBox(context);
                selectAllCheckBox.setText("Select All");
                selectAllCheckBox.setTypeface(null, Typeface.BOLD);
                selectAllCheckBox.setTextColor(ContextCompat.getColor(context, R.color.content_primary));
                selectAllCheckBox.setPadding(0, 8, 0, 8);
                selectAllCheckBox.setChecked(true); // Default checked
                updateCheckboxAppearance(context, selectAllCheckBox, true);
                collapsibleContent.addView(selectAllCheckBox);

                List<CheckBox> valueCheckBoxes = new ArrayList<>();

                // Individual value checkboxes
                for (String value : field.getPossibleValues()) {
                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setText(value);
                    checkBox.setTextColor(ContextCompat.getColor(context, R.color.content_primary));
                    checkBox.setPadding(20, 8, 0, 8); // Indent for individual items
                    checkBox.setChecked(true); // Default all checked

                    // Make checked boxes more visible
                    updateCheckboxAppearance(context, checkBox, true);

                    // Add listener to update appearance when state changes
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        updateCheckboxAppearance(context, checkBox, isChecked);

                        // Handle "Select All" logic
                        if (!isChecked) {
                            selectAllCheckBox.setChecked(false);
                            updateCheckboxAppearance(context, selectAllCheckBox, false);
                        } else {
                            // Check if all are selected
                            boolean allSelected = true;
                            for (CheckBox valueCb : valueCheckBoxes) {
                                if (!valueCb.isChecked()) {
                                    allSelected = false;
                                    break;
                                }
                            }
                            selectAllCheckBox.setChecked(allSelected);
                            updateCheckboxAppearance(context, selectAllCheckBox, allSelected);
                        }
                    });

                    collapsibleContent.addView(checkBox);
                    valueCheckBoxes.add(checkBox);
                    checkBoxes.add(checkBox);
                }

                // Set initial state of "Select All"
                selectAllCheckBox.setChecked(true);

                // Handle "Select All" checkbox
                selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    updateCheckboxAppearance(context, selectAllCheckBox, isChecked);
                    for (CheckBox cb : valueCheckBoxes) {
                        cb.setChecked(isChecked);
                        updateCheckboxAppearance(context, cb, isChecked);
                    }
                });

                // Handle individual checkboxes to update "Select All"
                for (CheckBox cb : valueCheckBoxes) {
                    cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        updateCheckboxAppearance(context, cb, isChecked);

                        if (!isChecked) {
                            selectAllCheckBox.setChecked(false);
                            updateCheckboxAppearance(context, selectAllCheckBox, false);
                        } else {
                            // Check if all are selected
                            boolean allSelected = true;
                            for (CheckBox valueCb : valueCheckBoxes) {
                                if (!valueCb.isChecked()) {
                                    allSelected = false;
                                    break;
                                }
                            }
                            selectAllCheckBox.setChecked(allSelected);
                            updateCheckboxAppearance(context, selectAllCheckBox, allSelected);
                        }
                    });
                }
            }

            // Set up click listener for collapsible functionality
            fieldHeader.setOnClickListener(v -> {
                if (collapsibleContent.getVisibility() == View.VISIBLE) {
                    // Collapse
                    collapsibleContent.setVisibility(View.GONE);
                    fieldHeader.setText("▶ " + field.getDisplayName()); // Right arrow (collapsed)
                } else {
                    // Expand
                    collapsibleContent.setVisibility(View.VISIBLE);
                    fieldHeader.setText("▼ " + field.getDisplayName()); // Down arrow (expanded)
                }
            });

            fieldCheckBoxes.add(checkBoxes);
        }

        builder.setView(scrollView);

        builder.setPositiveButton("Apply Filter", (dialog, which) -> {
            // Get global sort order
            GenericFilter.SortOrder globalSort = getSortOrderFromSpinner(globalSortSpinner);

            // Get selected values for each field
            GenericFilter.FilterCriteria[] criteria = new GenericFilter.FilterCriteria[fields.size()];

            for (int i = 0; i < fields.size(); i++) {
                String fieldName = fields.get(i).getFieldName();
                List<CheckBox> checkBoxes = fieldCheckBoxes.get(i);
                List<String> selectedValues = new ArrayList<>();

                // Get checked values
                for (CheckBox cb : checkBoxes) {
                    if (cb.isChecked()) {
                        selectedValues.add(cb.getText().toString());
                    }
                }

                criteria[i] = new GenericFilter.FilterCriteria(fieldName, GenericFilter.SortOrder.ALL);
                criteria[i].setSelectedValues(selectedValues);
            }

            callback.onFilterSelected(globalSort, criteria);
        });

        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Reset", (dialog, which) -> {
            GenericFilter.FilterCriteria[] resetCriteria = new GenericFilter.FilterCriteria[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                resetCriteria[i] = new GenericFilter.FilterCriteria(fields.get(i).getFieldName(), GenericFilter.SortOrder.ALL);
                resetCriteria[i].setSelectedValues(new ArrayList<>()); // Empty list means show all
            }
            callback.onFilterSelected(GenericFilter.SortOrder.ALL, resetCriteria);
        });

        AlertDialog dialog = builder.create();

        // Style the dialog buttons after creation
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.action_success));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.action_destructive));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.content_secondary));
        });

        dialog.show();
    }

    private static GenericFilter.SortOrder getSortOrderFromSpinner(Spinner spinner) {
        String selected = (String) spinner.getSelectedItem();
        switch (selected) {
            case "Ascending": return GenericFilter.SortOrder.ASCENDING;
            case "Descending": return GenericFilter.SortOrder.DESCENDING;
            default: return GenericFilter.SortOrder.ALL;
        }
    }

    private static void updateCheckboxAppearance(Context context, CheckBox checkBox, boolean isChecked) {
        if (isChecked) {
            // Subtle styling for checked boxes - more elegant
            checkBox.setBackgroundColor(ContextCompat.getColor(context, R.color.state_hover));
            checkBox.setTypeface(null, Typeface.BOLD);
            checkBox.setTextColor(ContextCompat.getColor(context, R.color.action_primary));
        } else {
            // Reset to default appearance for unchecked boxes
            checkBox.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
            checkBox.setTypeface(null, Typeface.NORMAL);
            checkBox.setTextColor(ContextCompat.getColor(context, R.color.content_secondary));
        }

        // Set fixed padding to avoid accumulation
        checkBox.setPadding(12, 8, 12, 8);
    }
}