package com.example.machinenote.Utility;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

        int padding = 16; // Slightly less padding for tighter highlight
        params.width = targetRect.width() + (padding * 2);
        params.height = targetRect.height() + (padding * 2);
        params.leftMargin = targetRect.left - padding;
        params.topMargin = targetRect.top - padding;

        // Add white glow effect behind the highlight
        highlightView.setElevation(10); // Raise above other elements

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

        // Measure the text container to know its height
        textContainer.measure(
                View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int textContainerHeight = textContainer.getMeasuredHeight();

        int margin = 20; // Reduced margin
        int safetyPadding = 10; // Extra padding to ensure no overlap

        // Check if there's more space below or above the target
        int spaceBelow = screenHeight - targetRect.bottom;
        int spaceAbove = targetRect.top;

        // Ensure text box doesn't cover the highlighted element
        if (spaceBelow > textContainerHeight + margin + safetyPadding) {
            // Position below target - plenty of space
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.topMargin = targetRect.bottom + margin;
            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.bottomMargin = 0;
        } else if (spaceAbove > textContainerHeight + margin + safetyPadding) {
            // Position above target - plenty of space
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.bottomMargin = screenHeight - targetRect.top + margin;
            params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.topMargin = 0;
        } else {
            // Not enough space on either side - position where there's more space
            // but ensure it doesn't overlap with target
            if (spaceBelow >= spaceAbove) {
                // Position below, but push further down if needed
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = targetRect.bottom + margin;
                params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.bottomMargin = 0;
            } else {
                // Position above, but push further up if needed
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.bottomMargin = screenHeight - targetRect.top + margin;
                params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = 0;
            }
        }

        // Center horizontally with margins
        params.leftMargin = 16;
        params.rightMargin = 16;

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