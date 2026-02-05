package com.example.machinenote.Utility;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.R;
import com.example.machinenote.models.DisplayableItem;
import com.example.machinenote.models.Narocila;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenericAdapter<T extends DisplayableItem> extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {

    private final Context context;
    private List<T> itemList;
    private final OnItemClickListener<T> onItemClickListener;
    private final List<String> fieldsToDisplay;
    private final Map<String, String> fieldDisplayNames;

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
        void onButtonClick(T item);
    }

    /**
     * Constructor with field names to display
     * @param context Context
     * @param itemList List of items
     * @param onItemClickListener Click listener
     * @param fieldsToDisplay List of field names to display in order
     */
    public GenericAdapter(Context context, List<T> itemList, OnItemClickListener<T> onItemClickListener, List<String> fieldsToDisplay) {
        this.context = context;
        this.itemList = itemList;
        this.onItemClickListener = onItemClickListener;
        this.fieldsToDisplay = fieldsToDisplay;
        this.fieldDisplayNames = new HashMap<>();
    }

    /**
     * Constructor with field names and custom display names
     * @param context Context
     * @param itemList List of items
     * @param onItemClickListener Click listener
     * @param fieldsToDisplay List of field names to display in order
     * @param fieldDisplayNames Map of field names to display names (e.g., "artikel" -> "Article Code")
     */
    public GenericAdapter(Context context, List<T> itemList, OnItemClickListener<T> onItemClickListener,
                          List<String> fieldsToDisplay, Map<String, String> fieldDisplayNames) {
        this.context = context;
        this.itemList = itemList;
        this.onItemClickListener = onItemClickListener;
        this.fieldsToDisplay = fieldsToDisplay;
        this.fieldDisplayNames = fieldDisplayNames != null ? fieldDisplayNames : new HashMap<>();
    }

    /**
     * Convenience method to create adapter with varargs
     */
    public static <T extends DisplayableItem> GenericAdapter<T> create(
            Context context, List<T> itemList, OnItemClickListener<T> onItemClickListener, String... fieldsToDisplay) {
        List<String> fieldList = new ArrayList<>();
        for (String field : fieldsToDisplay) {
            fieldList.add(field);
        }
        return new GenericAdapter<>(context, itemList, onItemClickListener, fieldList);
    }

    /**
     * Convenience method to create adapter with field mappings
     */
    public static <T extends DisplayableItem> GenericAdapter<T> createWithDisplayNames(
            Context context, List<T> itemList, OnItemClickListener<T> onItemClickListener,
            Map<String, String> fieldMappings) {
        List<String> fieldList = new ArrayList<>(fieldMappings.keySet());
        return new GenericAdapter<>(context, itemList, onItemClickListener, fieldList, fieldMappings);
    }

    public void updateList(List<T> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GenericAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_generic_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericAdapter.ViewHolder holder, int position) {
        T item = itemList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Get color based on status for Narocila items - using semantic color tokens
     */
    private int getStatusColor(String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.content_primary);

        String statusLower = status.toLowerCase().trim();
        switch (statusLower) {
            case "novo":
                return ContextCompat.getColor(context, R.color.soonColor); // Orange - urgent/new
            case "naročeno":
            case "naroceno":
                return ContextCompat.getColor(context, R.color.action_primary); // Primary blue - ordered
            case "v obdelavi":
            case "v_obdelavi":
                return ContextCompat.getColor(context, R.color.success_primary); // Green - in progress
            case "dostavljeno":
                return ContextCompat.getColor(context, R.color.finishedColor); // Gray - completed
            case "preklicano":
                return ContextCompat.getColor(context, R.color.error_primary); // Red - cancelled
            default:
                return ContextCompat.getColor(context, R.color.content_primary);
        }
    }

    /**
     * Get background color based on priority - using semantic color tokens
     */
    private int getBackgroundColor(T item) {
        // Only apply special background for Narocila items
        if (item instanceof Narocila) {
            Narocila narocilo = (Narocila) item;
            String status = narocilo.getStatus();

            if (status != null) {
                String statusLower = status.toLowerCase().trim();
                switch (statusLower) {
                    case "novo":
                        // Light tinted background for new items
                        return ContextCompat.getColor(context, R.color.surface_secondary);
                    case "naroceno":
                    case "naročeno":
                    case "v_obdelavi":
                    case "v obdelavi":
                        // Light tinted background for active items
                        return ContextCompat.getColor(context, R.color.surface_tertiary);
                    case "dostavljeno":
                        // Neutral background for completed items
                        return ContextCompat.getColor(context, R.color.background_section);
                    case "preklicano":
                        // Subtle error background for cancelled items
                        return ContextCompat.getColor(context, R.color.surface_secondary);
                }
            }
        }
        // Return transparent to use default background
        return ContextCompat.getColor(context, android.R.color.transparent);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        Button buttonAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            buttonAction = itemView.findViewById(R.id.buttonAction);
        }

        public void bind(T item) {
            container.removeAllViews(); // clear old views

            // Only modify background for Narocila items
            if (item instanceof Narocila) {
                int backgroundColor = getBackgroundColor(item);
                itemView.setBackgroundColor(backgroundColor);
            }
            // For non-Narocila items, don't modify background - keep the XML drawable

            Map<String, String> allFields = item.getDisplayFields();

            // Create filtered fields map in the specified order
            Map<String, String> filteredFields = new LinkedHashMap<>();

            for (String fieldName : fieldsToDisplay) {
                if (allFields.containsKey(fieldName)) {
                    String value = allFields.get(fieldName);
                    if (value != null && !value.trim().isEmpty()) {
                        // Use custom display name if available, otherwise use original field name
                        String displayName = fieldDisplayNames.getOrDefault(fieldName, fieldName);
                        filteredFields.put(displayName, value);
                    }
                }
            }

            // Create views dynamically based on filtered fields
            for (Map.Entry<String, String> entry : filteredFields.entrySet()) {
                String label = entry.getKey();
                String value = entry.getValue();

                // Create a horizontal layout for each field
                LinearLayout fieldLayout = new LinearLayout(context);
                fieldLayout.setOrientation(LinearLayout.HORIZONTAL);

                // Convert dp to pixels for padding
                int paddingVertical = (int) (4 * context.getResources().getDisplayMetrics().density);
                fieldLayout.setPadding(0, paddingVertical, 0, paddingVertical);

                // Label
                TextView labelView = new TextView(context);
                labelView.setText(label + ": ");
                labelView.setTextColor(ContextCompat.getColor(context, R.color.content_secondary));
                labelView.setTextSize(14);
                labelView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f));

                // Value
                TextView valueView = new TextView(context);
                valueView.setText(value);
                valueView.setTextSize(14);
                valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));

                // Apply special coloring for status field
                if (label.equalsIgnoreCase("Status") || label.equalsIgnoreCase("status")) {
                    valueView.setTextColor(getStatusColor(value));
                    // Make status text bold
                    valueView.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    valueView.setTextColor(ContextCompat.getColor(context, R.color.content_primary));
                }

                fieldLayout.addView(labelView);
                fieldLayout.addView(valueView);
                container.addView(fieldLayout);
            }

            buttonAction.setVisibility(View.GONE);
            itemView.setOnClickListener(v -> {
                AnimationHelper.bounceClick(v);
                onItemClickListener.onItemClick(item);
            });
            buttonAction.setOnClickListener(v -> {
                AnimationHelper.bounceClick(v);
                onItemClickListener.onButtonClick(item);
            });
        }
    }
}