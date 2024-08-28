package com.example.machinenote.Utility;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.machinenote.activities.MainActivity;

public class KeyboardUtils {

    // Hide the keyboard
    public static void hideKeyboard(Context context) {
        View view = ((MainActivity) context).getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}