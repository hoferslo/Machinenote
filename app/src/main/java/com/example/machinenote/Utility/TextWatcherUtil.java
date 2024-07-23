package com.example.machinenote.Utility;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.example.machinenote.models.ListViewItem;

import java.util.List;

public class TextWatcherUtil {

    public static void addTextWatcherToEditText(Context context, EditText editText, List<ListViewItem> data, int number, ListViewAdapter adapter) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                boolean isCompleted = !text.isEmpty();
                adapter.updateItemStatus(number, isCompleted); // Update item status based on input
            }
        });
    }

}
