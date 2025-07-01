package com.example.machinenote.Utility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.machinenote.R;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.models.ListViewItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<ListViewItem> {

    private int resourceLayout;
    private Context context;
    private List<ListViewItem> allItems; // Store all items
    private List<ListViewItem> visibleItems; // Store only visible items

    public ListViewAdapter(Context context, int resource, List<ListViewItem> items) {
        super(context, resource);
        this.resourceLayout = resource;
        this.context = context;
        this.allItems = items;
        this.visibleItems = new ArrayList<>();
        updateVisibleItems();
    }

    private void updateVisibleItems() {
        visibleItems.clear();
        for (ListViewItem item : allItems) {
            if (item.isVisible()) {
                visibleItems.add(item);
            }
        }
    }

    @Override
    public int getCount() {
        return visibleItems.size();
    }

    @Override
    public ListViewItem getItem(int position) {
        return visibleItems.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resourceLayout, parent, false);
        }

        ListViewItem item = getItem(position);
        TextView textViewItem = convertView.findViewById(R.id.customTextView);

        textViewItem.setText(item.getName());

        Drawable drawable;
        if (item.isCompleted()) {
            textViewItem.setTextColor(Color.GREEN);
            drawable = ContextCompat.getDrawable(context, R.mipmap.check_circle);
            if (drawable != null) {
                drawable.setColorFilter(ContextCompat.getColor(context, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        } else {
            textViewItem.setTextColor(Color.RED);
            drawable = ContextCompat.getDrawable(context, R.mipmap.error);
            if (drawable != null) {
                drawable.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        // Set the drawable on the left
        textViewItem.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        return convertView;
    }

    public boolean areAllItemsComplete() {
        for (ListViewItem item : visibleItems) { // Check only visible items
            if(item.isVisible()){
                if (item.getNumber() == 999) {
                    Toast.makeText(context, context.getString(R.string.time_negative_error), Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).showLoadingBar(false, "");
                    return false;
                }
                if (!item.isCompleted()) {
                    Toast.makeText(context, context.getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).showLoadingBar(false, "");
                    return false;
                }
            }
        }
        return true;
    }
 //
    public void sortList() {
        Collections.sort(visibleItems, new Comparator<ListViewItem>() {
            @Override
            public int compare(ListViewItem o1, ListViewItem o2) {
                // Compare by completion status: Uncompleted items first
                int compareCompletion = Boolean.compare(o1.isCompleted(), o2.isCompleted());
                if (compareCompletion != 0) {
                    return compareCompletion;
                } else {
                    // Compare by number: Ascending order
                    return Integer.compare(o1.getNumber(), o2.getNumber());
                }
            }
        });
        notifyDataSetChanged();
    }

    public void updateItemStatus(int number, boolean isCompleted) {
        for (ListViewItem item : allItems) { // Search in all items
            if (item.getNumber() == number) {
                item.setCompleted(isCompleted);
                break;
            }
        }
        updateVisibleItems(); // Update visible items list
        sortList();
    }

    public void updateErrorItemVisibility(int number, boolean showError) {
        for (ListViewItem item : allItems) { // Search in all items
            if (item.getNumber() == number) {
                // Hide item if showError is true, show if false
                item.setVisible(showError);
                break;
            }
        }
        updateVisibleItems(); // Update visible items list
        sortList();
    }
}