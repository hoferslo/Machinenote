package com.example.machinenote.Utility;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextWatcherUtil {

    public static void addTextWatcherToEditText(EditText editText, int number, ListViewAdapter adapter) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                boolean isCompleted = !text.isEmpty();
                adapter.updateItemStatus(number, isCompleted); // Update item status based on input
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

    public static void handleHeightOfStoppages(Context context, LinearLayout Ll) {
        Ll.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Remove the listener to avoid multiple calls
                        Ll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int height = Ll.getHeight();

                        if (height < Ll.getMinimumHeight()) {
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (int) (100 * context.getResources().getDisplayMetrics().density) // Convert 64dp to pixels
                            );
                            int marginBottom = (int) (8 * context.getResources().getDisplayMetrics().density); // Convert 16dp to pixels
                            layoutParams.setMargins(0, 0, 0, marginBottom);
                            Ll.setLayoutParams(layoutParams);
                        }
                    }
                }
        );
    }
}
