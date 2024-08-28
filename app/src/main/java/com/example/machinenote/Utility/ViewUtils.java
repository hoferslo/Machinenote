package com.example.machinenote.Utility;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.machinenote.R;

public class ViewUtils {

    public static LinearLayout createTextViewLinearLayoutStartEnd(Context context, String startText, String endText) {
        // Create a LinearLayout
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams LlParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT // Adjusted to WRAP_CONTENT for better sizing
        );
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        int marginInDp = 4; // 8dp margin
        int marginInPixels = dpToPx(context, marginInDp);
        LlParams.setMargins(0, 0, 0, marginInPixels);

        // Define padding in dp
        int paddingInDp = 8; // 16dp padding
        int paddingInPixels = dpToPx(context, paddingInDp);
        linearLayout.setPadding(paddingInPixels, paddingInPixels / 2, paddingInPixels, paddingInPixels / 2);

        linearLayout.setLayoutParams(LlParams);
        setLinearLayoutBackground(context, linearLayout);

        // Create the first TextView with the custom style
        TextView startTextView = new TextView(context);

        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        startTextView.setLayoutParams(startParams);
        startTextView.setText(startText);

        // Create the second TextView with the custom style
        TextView endTextView = new TextView(context);

        LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // weight
        );

        endTextView.setLayoutParams(endParams);
        endTextView.setText(endText);
        endTextView.setGravity(Gravity.END);

        // Add the TextViews to the LinearLayout
        linearLayout.addView(startTextView);
        linearLayout.addView(endTextView);

        return linearLayout;
    }


    private static void setLinearLayoutBackground(Context context, LinearLayout linearLayout) {
        // You can set a background drawable or color
        // For example, setting a drawable background
        Drawable backgroundDrawable = context.getDrawable(R.drawable.background_corners_50);
        linearLayout.setBackground(backgroundDrawable);

        // Alternatively, you can set a color background
        // linearLayout.setBackgroundColor(context.getResources().getColor(R.color.some_color));
    }

    // Method to convert dp to pixels
    private static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }
}
