package com.example.machinenote.Utility;

import android.content.Context;
import android.graphics.Color;
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
     * Get color based on status for Narocila items
     */
    private int getStatusColor(String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.content_primary);

        String statusLower = status.toLowerCase().trim();
        switch (statusLower) {
            case "novo":
                return Color.parseColor("#FF6B35"); // Orange - urgent/new
            case "naročeno":
                return Color.parseColor("#4169E1"); // Blue - ordered
            case "v obdelavi":
                return Color.parseColor("#32CD32"); // Green - in progress
            case "dostavljeno":
                return Color.parseColor("#808080"); // Gray - completed
            case "preklicano":
                return Color.parseColor("#DC143C"); // Red - cancelled
            default:
                return ContextCompat.getColor(context, R.color.content_primary);
        }
    }

    /**
     * Get background color based on priority
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
                        return Color.parseColor("#FFF3E0"); // Light orange background
                    case "naroceno":
                    case "v_obdelavi":
                        return Color.parseColor("#E8F5E8"); // Light green background
                    case "dostavljeno":
                        return Color.parseColor("#F5F5F5"); // Light gray background
                    case "preklicano":
                        return Color.parseColor("#FFEBEE"); // Light red background
                }
            }
        }
        return Color.TRANSPARENT;
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
                Integer backgroundColor = getBackgroundColor(item);
                if (backgroundColor != null) {
                    // Apply custom color for specific status
                    itemView.setBackgroundColor(backgroundColor);
                } else {
                    // Reset to default for Narocila with unknown status
                    itemView.setBackground(null);
                }
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
                fieldLayout.setPadding(0, 4, 0, 4);

                // Label
                TextView labelView = new TextView(context);
                labelView.setText(label + ": ");
                labelView.setTextColor(ContextCompat.getColorStateList(context, R.color.content_secondary));
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
                    valueView.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
                }

                fieldLayout.addView(labelView);
                fieldLayout.addView(valueView);
                container.addView(fieldLayout);
            }

            buttonAction.setVisibility(View.GONE);
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
            buttonAction.setOnClickListener(v -> onItemClickListener.onButtonClick(item));
        }
    }
}