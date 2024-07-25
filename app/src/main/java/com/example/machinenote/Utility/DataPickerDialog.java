package com.example.machinenote.Utility;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.example.machinenote.R;

public class DataPickerDialog {

    private static int whichStringCache;

    public static int showDialog(View view, String title, String[] items, Context context, Button button) {
        return showDialog(view, title, items, context, button, null, -1);
    }

    public static int showDialog(View view, String title, String[] items, Context context, Button button, ListViewAdapter adapter, int whichListItem) {
        whichStringCache = -1;

        // Inflate the default layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(android.R.layout.select_dialog_item, null);

        // Set up the AlertDialog with custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builder.setTitle(title)
                .setView(dialogView)
                .setItems(items, (dialog, which) -> {
                    button.setText(items[which]);
                    whichStringCache = which;
                    if (adapter != null && whichListItem != -1) {
                        adapter.updateItemStatus(whichListItem, true);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        return whichStringCache;
    }
}
