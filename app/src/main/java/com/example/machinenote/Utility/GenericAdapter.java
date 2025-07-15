package com.example.machinenote.Utility;

import android.content.Context;
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

import java.util.List;
import java.util.Map;

public class GenericAdapter<T extends DisplayableItem> extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {

    private final Context context;
    private List<T> itemList;
    private final OnItemClickListener<T> onItemClickListener;

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
        void onButtonClick(T item);
    }

    public GenericAdapter(Context context, List<T> itemList, OnItemClickListener<T> onItemClickListener) {
        this.context = context;
        this.itemList = itemList;
        this.onItemClickListener = onItemClickListener;
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

            Map<String, String> fields = item.getDisplayFields();

            // Create views dynamically based on fields
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                String label = entry.getKey();
                String value = entry.getValue();

                if (value == null || value.isEmpty()) continue;

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
                valueView.setTextColor(ContextCompat.getColorStateList(context, R.color.content_primary));
                valueView.setTextSize(14);
                valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));

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