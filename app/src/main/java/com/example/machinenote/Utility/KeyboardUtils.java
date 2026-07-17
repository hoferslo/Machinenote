package com.example.machinenote.Utility;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.machinenote.activities.MainActivity;

public class KeyboardUtils {

    // Hide the keyboard safely
    public static void hideKeyboard(Context context) {
        if (context == null) {
            return;
        }

        // Get the currently focused view
        View view = ((MainActivity) context).getCurrentFocus();

        // Only attempt to hide the keyboard if we actually have a focused view
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}