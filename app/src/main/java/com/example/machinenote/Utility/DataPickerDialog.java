package com.example.machinenote.Utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.example.machinenote.R;

public class DataPickerDialog {

    private static int whichStringCache;

    public static int showDialog(View view, String title, String[] items, Context context, Button button, OnItemSelectedListener listener) {
        return showDialog(view, title, items, context, button, null, -1, listener);
    }

    public static int showDialog(View view, String title, String[] items, Context context, Button button, ListViewAdapter adapter, int whichListItem, OnItemSelectedListener listener) {
        whichStringCache = -1;


        // Set up the AlertDialog with custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builder.setTitle(title)
                .setItems(items, (dialog, which) -> {
                    button.setText(items[which]);
                    whichStringCache = which;
                    if (adapter != null && whichListItem != -1) {
                        adapter.updateItemStatus(whichListItem, true);
                        listener.onItemSelected(whichStringCache); //kle je bla napaka,
                        // men se je skor odtrgal, zakva je biu klele whichListItem, enga bom pretepu
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        return whichStringCache;
    }


    public interface OnItemSelectedListener {
        void onItemSelected(int selectedIndex);
    }
}
