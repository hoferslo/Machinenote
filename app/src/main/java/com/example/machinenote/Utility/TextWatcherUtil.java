package com.example.machinenote.Utility;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.machinenote.models.ListViewItem;

import java.util.List;

public class TextWatcherUtil {

    public static void addTextWatcherToEditText(EditText editText, int number, ListViewAdapter adapter) {
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

    public static void addTextWatcherToEditText(EditText editText, TextView textView, int number, ListViewAdapter adapter) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the TextView with the current text from EditText
                textView.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                boolean isCompleted = !text.isEmpty();

                // Update the ListView item status based on the input
                adapter.updateItemStatus(number, isCompleted);

                // Optionally, you can set the TextView's text again here, though it's already done in onTextChanged
                // textView.setText(text);
            }
        });
    }
    public static void addTextWatcherToTextView(TextView textView, int number, ListViewAdapter adapter) {
        // Add a TextWatcher to the TextView (requires a workaround)
        textView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                String text = textView.getText().toString().trim();
                boolean isCompleted = !text.isEmpty();
                adapter.updateItemStatus(number, isCompleted); // Update item status based on input
            }
        });
    }
}
