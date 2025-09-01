package com.example.machinenote.Utility;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GenericFilter<T> {

    public enum SortOrder {
        ASCENDING, DESCENDING, ALL
    }

    public static class FilterCriteria {
        private String fieldName;
        private SortOrder sortOrder;
        private String specificValue; // for filtering by specific values like "skladišče 1", "skladišče 2"
        private List<String> selectedValues; // for multiple checkbox selections

        public FilterCriteria(String fieldName, SortOrder sortOrder) {
            this.fieldName = fieldName;
            this.sortOrder = sortOrder;
            this.selectedValues = new ArrayList<>();
        }

        public FilterCriteria(String fieldName, SortOrder sortOrder, String specificValue) {
            this.fieldName = fieldName;
            this.sortOrder = sortOrder;
            this.specificValue = specificValue;
            this.selectedValues = new ArrayList<>();
        }

        // Getters
        public String getFieldName() { return fieldName; }
        public SortOrder getSortOrder() { return sortOrder; }
        public String getSpecificValue() { return specificValue; }
        public List<String> getSelectedValues() { return selectedValues; }

        // Setters
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        public void setSortOrder(SortOrder sortOrder) { this.sortOrder = sortOrder; }
        public void setSpecificValue(String specificValue) { this.specificValue = specificValue; }
        public void setSelectedValues(List<String> selectedValues) { this.selectedValues = selectedValues; }
    }

    public interface FilterCallback<T> {
        void onFilterApplied(List<T> filteredList);
    }

    private List<T> originalList;
    private List<T> currentFilteredList;
    private Map<String, Function<T, String>> fieldExtractors;
    private FilterCallback<T> callback;
    private Function<T, String> nameExtractor; // For global sorting by name

    public GenericFilter(List<T> dataList, FilterCallback<T> callback) {
        this.originalList = new ArrayList<>(dataList);
        this.currentFilteredList = new ArrayList<>(dataList);
        this.fieldExtractors = new HashMap<>();
        this.callback = callback;
    }

    // Register field extractors for different fields
    public GenericFilter<T> addFieldExtractor(String fieldName, Function<T, String> extractor) {
        fieldExtractors.put(fieldName, extractor);
        return this;
    }

    // Register the name extractor for global sorting
    public GenericFilter<T> setNameExtractor(Function<T, String> nameExtractor) {
        this.nameExtractor = nameExtractor;
        return this;
    }

    // Apply filter with global sort order and multiple criteria
    public void applyFilter(SortOrder globalSortOrder, FilterCriteria... criteria) {
        List<T> result = new ArrayList<>(originalList);

        // Apply field filters first
        for (FilterCriteria criterion : criteria) {
            result = applyFieldFilter(result, criterion);
        }

        // Apply global sorting by name at the end
        if (globalSortOrder != SortOrder.ALL && nameExtractor != null) {
            switch (globalSortOrder) {
                case ASCENDING:
                    result.sort((a, b) -> {
                        String nameA = nameExtractor.apply(a);
                        String nameB = nameExtractor.apply(b);
                        if (nameA == null) nameA = "";
                        if (nameB == null) nameB = "";
                        return nameA.compareToIgnoreCase(nameB);
                    });
                    break;
                case DESCENDING:
                    result.sort((a, b) -> {
                        String nameA = nameExtractor.apply(a);
                        String nameB = nameExtractor.apply(b);
                        if (nameA == null) nameA = "";
                        if (nameB == null) nameB = "";
                        return nameB.compareToIgnoreCase(nameA);
                    });
                    break;
            }
        }

        this.currentFilteredList = result;
        if (callback != null) {
            callback.onFilterApplied(new ArrayList<>(result));
        }
    }

    // Backward compatibility method
    public void applyFilter(FilterCriteria... criteria) {
        applyFilter(SortOrder.ALL, criteria);
    }

    private List<T> applyFieldFilter(List<T> list, FilterCriteria criteria) {
        String fieldName = criteria.getFieldName();
        Function<T, String> extractor = fieldExtractors.get(fieldName);

        if (extractor == null) {
            return list; // If no extractor found, return original list
        }

        List<T> filtered = list;

        // Filter by selected values from checkboxes
        if (criteria.getSelectedValues() != null && !criteria.getSelectedValues().isEmpty()) {
            filtered = list.stream()
                    .filter(item -> {
                        String value = extractor.apply(item);
                        if (value == null) value = "";

                        // Check if the item's value is in the selected values list
                        for (String selectedValue : criteria.getSelectedValues()) {
                            if (value.toLowerCase().contains(selectedValue.toLowerCase())) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        // Fallback to specific value filtering (for backward compatibility)
        else if (criteria.getSpecificValue() != null && !criteria.getSpecificValue().isEmpty()) {
            filtered = list.stream()
                    .filter(item -> {
                        String value = extractor.apply(item);
                        return value != null && value.toLowerCase().contains(criteria.getSpecificValue().toLowerCase());
                    })
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    // Get unique values for a specific field (useful for creating dropdown options)
    public List<String> getUniqueValuesForField(String fieldName) {
        Function<T, String> extractor = fieldExtractors.get(fieldName);
        if (extractor == null) {
            return new ArrayList<>();
        }

        return originalList.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .filter(value -> !value.trim().isEmpty()) // Filter out empty strings
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    // Reset to original list
    public void resetFilter() {
        this.currentFilteredList = new ArrayList<>(originalList);
        if (callback != null) {
            callback.onFilterApplied(new ArrayList<>(currentFilteredList));
        }
    }

    // Update the original data
    public void updateData(List<T> newData) {
        this.originalList = new ArrayList<>(newData);
        this.currentFilteredList = new ArrayList<>(newData);
    }

    // Get current filtered list
    public List<T> getCurrentFilteredList() {
        return new ArrayList<>(currentFilteredList);
    }

    // Get original list
    public List<T> getOriginalList() {
        return new ArrayList<>(originalList);
    }
}