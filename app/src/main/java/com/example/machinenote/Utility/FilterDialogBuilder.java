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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Filter Options");

        // Main scroll view for the dialog
        ScrollView scrollView = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        scrollView.addView(layout);

        // Global Sort Section
        TextView sortLabel = new TextView(context);
        sortLabel.setText("Sort All Items By Name:");
        sortLabel.setTextSize(16);
        sortLabel.setTypeface(null, Typeface.BOLD);
        sortLabel.setPadding(0, 0, 0, 10);
        layout.addView(sortLabel);

        Spinner globalSortSpinner = new Spinner(context);
        String[] sortOptions = {"No Sorting", "Ascending", "Descending"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        globalSortSpinner.setAdapter(sortAdapter);
        layout.addView(globalSortSpinner);

        // Add separator
        TextView separator = new TextView(context);
        separator.setText("━━━━━━━━━━━━━━━━━━━━━");
        separator.setPadding(0, 20, 0, 20);
        separator.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        layout.addView(separator);

        // Store checkbox references for each field
        List<List<CheckBox>> fieldCheckBoxes = new ArrayList<>();

        // Create collapsible sections for each field
        for (int i = 0; i < fields.size(); i++) {
            FilterField field = fields.get(i);

            // Create collapsible header with arrow
            TextView fieldHeader = new TextView(context);
            fieldHeader.setText("▼ " + field.getDisplayName()); // Down arrow initially (expanded)
            fieldHeader.setTextSize(16);
            fieldHeader.setTypeface(null, Typeface.BOLD);
            fieldHeader.setPadding(0, 20, 0, 10);
            fieldHeader.setClickable(true);
            fieldHeader.setFocusable(true);

            // Add visual feedback for clickable header
            fieldHeader.setPadding(10, 10, 10, 10); // Add some padding for better touch target

            layout.addView(fieldHeader);

            // Create container for the collapsible content
            LinearLayout collapsibleContent = new LinearLayout(context);
            collapsibleContent.setOrientation(LinearLayout.VERTICAL);
            collapsibleContent.setPadding(20, 0, 0, 0); // Indent the content
            layout.addView(collapsibleContent);

            // Create checkboxes for each possible value
            List<CheckBox> checkBoxes = new ArrayList<>();
            if (field.getPossibleValues() != null && !field.getPossibleValues().isEmpty()) {

                // "Select All" checkbox
                CheckBox selectAllCheckBox = new CheckBox(context);
                selectAllCheckBox.setText("Select All");
                selectAllCheckBox.setTypeface(null, Typeface.BOLD);
                selectAllCheckBox.setPadding(0, 5, 0, 5);
                selectAllCheckBox.setChecked(true); // Default checked
                updateCheckboxAppearance(selectAllCheckBox, true);
                collapsibleContent.addView(selectAllCheckBox);

                List<CheckBox> valueCheckBoxes = new ArrayList<>();

                // Individual value checkboxes
                for (String value : field.getPossibleValues()) {
                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setText(value);
                    checkBox.setPadding(20, 5, 0, 5); // Indent for individual items
                    checkBox.setChecked(true); // Default all checked

                    // Make checked boxes more visible
                    updateCheckboxAppearance(checkBox, true);

                    // Add listener to update appearance when state changes
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        updateCheckboxAppearance(checkBox, isChecked);

                        // Handle "Select All" logic
                        if (!isChecked) {
                            selectAllCheckBox.setChecked(false);
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
                    updateCheckboxAppearance(selectAllCheckBox, isChecked);
                    for (CheckBox cb : valueCheckBoxes) {
                        cb.setChecked(isChecked);
                        updateCheckboxAppearance(cb, isChecked);
                    }
                });

                // Handle individual checkboxes to update "Select All"
                for (CheckBox cb : valueCheckBoxes) {
                    cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        updateCheckboxAppearance(cb, isChecked);

                        if (!isChecked) {
                            selectAllCheckBox.setChecked(false);
                            updateCheckboxAppearance(selectAllCheckBox, false);
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
                            updateCheckboxAppearance(selectAllCheckBox, allSelected);
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

        builder.show();
    }

    private static GenericFilter.SortOrder getSortOrderFromSpinner(Spinner spinner) {
        String selected = (String) spinner.getSelectedItem();
        switch (selected) {
            case "Ascending": return GenericFilter.SortOrder.ASCENDING;
            case "Descending": return GenericFilter.SortOrder.DESCENDING;
            default: return GenericFilter.SortOrder.ALL;
        }
    }

    private static void updateCheckboxAppearance(CheckBox checkBox, boolean isChecked) {
        if (isChecked) {
            // Make checked boxes more visible with background color and bold text
            checkBox.setBackgroundColor(0xFFE3F2FD); // Light blue background
            checkBox.setTypeface(null, Typeface.BOLD);
            checkBox.setTextColor(0xFF1976D2); // Blue text
        } else {
            // Reset to default appearance for unchecked boxes
            checkBox.setBackgroundColor(0x00000000); // Transparent background
            checkBox.setTypeface(null, Typeface.NORMAL);
            checkBox.setTextColor(0xFF000000); // Black text
        }

        // Add some padding to make the background visible
        checkBox.setPadding(checkBox.getPaddingLeft() + 8,
                checkBox.getPaddingTop() + 4,
                checkBox.getPaddingRight() + 8,
                checkBox.getPaddingBottom() + 4);
    }
}