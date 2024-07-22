package com.example.machinenote.Utility;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.machinenote.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<ListViewItem> {

    private int resourceLayout;
    private Context context;
    private List<ListViewItem> items;

    public ListViewAdapter(Context context, int resource, List<ListViewItem> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resourceLayout, parent, false);
        }

        ListViewItem item = getItem(position);
        TextView textViewItem = convertView.findViewById(R.id.customTextView);

        textViewItem.setText(item.getName());

        if (item.isCompleted()) {
            textViewItem.setTextColor(Color.GREEN);
        } else {
            textViewItem.setTextColor(Color.RED);
        }
        return convertView;
    }

    public void sortList() {
        Collections.sort(items, new Comparator<ListViewItem>() {
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
        for (ListViewItem item : items) {
            if (item.getNumber() == number) {
                item.setCompleted(isCompleted);
                break;
            }
        }
        sortList();
    }

}
