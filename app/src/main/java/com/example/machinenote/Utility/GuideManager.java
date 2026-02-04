package com.example.machinenote.Utility;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.machinenote.R;

import java.util.ArrayList;
import java.util.List;

/**
 * GuideManager - Manages step-by-step visual guides for the app
 * Shows hints one at a time with Next/Previous navigation
 */
public class GuideManager {

    private Activity activity;
    private List<GuideStep> steps;
    private int currentStep = 0;
    private ViewGroup overlayContainer;
    private View dimOverlay;
    private View guideLayout;
    private OnGuideCompleteListener completeListener;

    public interface OnGuideCompleteListener {
        void onGuideComplete();
    }

    public GuideManager(Activity activity) {
        this.activity = activity;
        this.steps = new ArrayList<>();
    }

    /**
     * Add a guide step
     * @param targetView The view to highlight
     * @param title Title of the hint
     * @param description Description text
     */
    public GuideManager addStep(View targetView, String title, String description) {
        steps.add(new GuideStep(targetView, title, description));
        return this;
    }

    /**
     * Set listener for when guide is completed
     */
    public GuideManager setOnCompleteListener(OnGuideCompleteListener listener) {
        this.completeListener = listener;
        return this;
    }

    /**
     * Start the guide from the first step
     */
    public void start() {
        if (steps.isEmpty()) {
            return;
        }

        currentStep = 0;
        createOverlay();
        showStep(currentStep);
    }

    /**
     * Create the overlay container and dim background
     */
    private void createOverlay() {
        // Get root view
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();

        // Create overlay container
        overlayContainer = new RelativeLayout(activity);
        overlayContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlayContainer.setClickable(true);

        // Create dim overlay - TRANSPARENT for light background
        dimOverlay = new View(activity);
        RelativeLayout.LayoutParams dimParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        dimOverlay.setLayoutParams(dimParams);
        dimOverlay.setBackgroundColor(Color.parseColor("#40000000")); // Very light dim (25% opacity)
        dimOverlay.setClickable(false);

        overlayContainer.addView(dimOverlay);

        // Add overlay to root view
        rootView.addView(overlayContainer);
    }

    /**
     * Show a specific step
     */
    private void showStep(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            finish();
            return;
        }

        // Remove previous guide layout if exists
        if (guideLayout != null && guideLayout.getParent() != null) {
            ((ViewGroup) guideLayout.getParent()).removeView(guideLayout);
        }

        GuideStep step = steps.get(stepIndex);
        View targetView = step.targetView;

        // Make sure target view is visible and properly laid out
        if (targetView.getVisibility() != View.VISIBLE) {
            targetView.setVisibility(View.VISIBLE);
        }

        // Force layout if needed
        if (!targetView.isLaidOut()) {
            targetView.post(() -> showStep(stepIndex));
            return;
        }

        // Get target view position
        int[] location = new int[2];
        targetView.getLocationOnScreen(location);
        Rect targetRect = new Rect(
                location[0],
                location[1],
                location[0] + targetView.getWidth(),
                location[1] + targetView.getHeight()
        );

        // Create guide layout
        LayoutInflater inflater = LayoutInflater.from(activity);
        guideLayout = inflater.inflate(R.layout.guide_overlay, overlayContainer, false);

        // Set up guide content
        TextView titleText = guideLayout.findViewById(R.id.guide_title);
        TextView descText = guideLayout.findViewById(R.id.guide_description);
        TextView stepCounter = guideLayout.findViewById(R.id.guide_step_counter);
        Button nextButton = guideLayout.findViewById(R.id.guide_next_btn);
        Button prevButton = guideLayout.findViewById(R.id.guide_prev_btn);
        Button skipButton = guideLayout.findViewById(R.id.guide_skip_btn);
        View highlightView = guideLayout.findViewById(R.id.guide_highlight);

        View textContainer = guideLayout.findViewById(R.id.guide_text_container);

// highlight je nad dimom
        highlightView.setElevation(10f);

// text box mora biti NAD highlightom
        textContainer.setElevation(30f);


        titleText.setText(step.title);
        descText.setText(step.description);
        stepCounter.setText((currentStep + 1) + " / " + steps.size());

        // Position highlight around target view
        positionHighlight(highlightView, targetRect);

        // Position guide text (below or above target)
        positionGuideText(guideLayout, targetRect);

        // Set up button visibility and listeners
        prevButton.setVisibility(currentStep > 0 ? View.VISIBLE : View.GONE);
        nextButton.setText(currentStep == steps.size() - 1 ? "Done" : "Next");

        prevButton.setOnClickListener(v -> {
            currentStep--;
            showStep(currentStep);
        });

        nextButton.setOnClickListener(v -> {
            if (currentStep == steps.size() - 1) {
                finish();
            } else {
                currentStep++;
                showStep(currentStep);
            }
        });

        skipButton.setOnClickListener(v -> finish());

        overlayContainer.addView(guideLayout);

        // Don't bring target view to front - let it stay in its position
        // This prevents buttons from moving around
    }

    /**
     * Position the highlight view around the target
     */
    private void positionHighlight(View highlightView, Rect targetRect) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) highlightView.getLayoutParams();

        int padding = (int) activity.getResources().getDimension(R.dimen.spacing_xl);
        params.width = targetRect.width() + (padding * 2);
        params.height = targetRect.height() + (padding * 2);
        params.leftMargin = targetRect.left - padding;
        params.topMargin = targetRect.top - padding;

        // Add strong elevation for visibility
        highlightView.setElevation(16); // Povečaj iz 10 na 16
        highlightView.setOutlineProvider(android.view.ViewOutlineProvider.BACKGROUND);
        highlightView.setClipToOutline(false); // Pomembno za glow effect

        highlightView.setLayoutParams(params);
    }

    /**
     * Position the guide text box (below or above target based on space)
     */
    private void positionGuideText(View guideLayout, Rect targetRect) {
        View textContainer = guideLayout.findViewById(R.id.guide_text_container);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textContainer.getLayoutParams();

        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;

        int spacingXl = (int) activity.getResources().getDimension(R.dimen.spacing_xl);
        int spacingLarge = (int) activity.getResources().getDimension(R.dimen.spacing_large);
        int highlightPadding = spacingXl; // Same as positionHighlight

        // Measure the text container to know its height
        int horizontalMargins = spacingXl * 2;
        textContainer.measure(
                View.MeasureSpec.makeMeasureSpec(screenWidth - horizontalMargins, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int textContainerHeight = textContainer.getMeasuredHeight();

        // Calculate actual space available
        int spaceBelow = screenHeight - (targetRect.bottom + highlightPadding);
        int spaceAbove = targetRect.top - highlightPadding;

        // Calculate minimum space needed (text height + margin for breathing room)
        int minSpaceNeeded = textContainerHeight + spacingLarge;

        // Clear all positioning rules first
        params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.topMargin = 0;
        params.bottomMargin = 0;

        // Position based on available space
        if (spaceBelow >= minSpaceNeeded) {
            // Plenty of space below - position below the target
            params.topMargin = targetRect.bottom + highlightPadding + spacingLarge;

        } else if (spaceAbove >= minSpaceNeeded) {
            // Plenty of space above - position above the target

            params.topMargin = targetRect.top - highlightPadding - spacingLarge - textContainerHeight;

        } else {
            // Tight on space - use whichever side has more room
            if (spaceAbove > spaceBelow) {
                // Position above, push toward top if needed
                int maxTopMargin = Math.max(spacingXl, targetRect.top - highlightPadding - textContainerHeight - spacingLarge);
                params.topMargin = maxTopMargin;
            } else {
                // Position below, allow it to use available space
                params.topMargin = targetRect.bottom + highlightPadding + spacingLarge;
            }
        }

        // Horizontal margins
        params.leftMargin = spacingXl;
        params.rightMargin = spacingXl;

        textContainer.setLayoutParams(params);
    }

    /**
     * Finish and clean up the guide
     */
    private void finish() {
        if (overlayContainer != null && overlayContainer.getParent() != null) {
            ((ViewGroup) overlayContainer.getParent()).removeView(overlayContainer);
        }

        if (completeListener != null) {
            completeListener.onGuideComplete();
        }
    }

    /**
     * Data class for a guide step
     */
    private static class GuideStep {
        View targetView;
        String title;
        String description;

        GuideStep(View targetView, String title, String description) {
            this.targetView = targetView;
            this.title = title;
            this.description = description;
        }
    }
}