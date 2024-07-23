package com.example.machinenote.Utility;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class DataPickerDialog {

    private static int whichStringCache;

    public static int showDialog(View view, String title, String[] items, Context context, Button button) {
        return showDialog(view, title, items, context, button, null, -1);
    }

    public static int showDialog(View view, String title, String[] items, Context context, Button button, ListViewAdapter adapter, int whichListItem) {
        whichStringCache = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setItems(items, (dialog, which) -> {
                    button.setText(items[which]);
                    whichStringCache = which;
                    if (adapter != null && whichListItem != -1) {
                        Log.d(TAG, String.valueOf(whichStringCache));
                        adapter.updateItemStatus(whichListItem, true);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        return whichStringCache;
    }
}
