package com.example.machinenote.Utility;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
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

        // Create dim overlay with cutout
        dimOverlay = new DimOverlayView(activity);
        RelativeLayout.LayoutParams dimParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        dimOverlay.setLayoutParams(dimParams);
        dimOverlay.setClickable(false);

        overlayContainer.addView(dimOverlay);

        // Add overlay container to root
        rootView.addView(overlayContainer);

        // Animate overlay fade in using AnimationHelper
        AnimationHelper.fadeIn(overlayContainer, 300);
    }


    /**
     * Show a specific step
     */
    private void showStep(int stepIndex) {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            finish();
            return;
        }

        // Remove previous guide layout if exists with fade out
        if (guideLayout != null && guideLayout.getParent() != null) {
            View oldGuideLayout = guideLayout;
            AnimationHelper.fadeOut(oldGuideLayout, 200);
            oldGuideLayout.postDelayed(() -> {
                if (oldGuideLayout.getParent() != null) {
                    ((ViewGroup) oldGuideLayout.getParent()).removeView(oldGuideLayout);
                }
                // Show new step after old one fades out
                showStepInternal(stepIndex);
            }, 200);
        } else {
            // No previous layout, show immediately
            showStepInternal(stepIndex);
        }
    }

    /**
     * Internal method to actually show the step
     */
    private void showStepInternal(int stepIndex) {
        GuideStep step = steps.get(stepIndex);
        View targetView = step.targetView;

        // Make sure target view is visible and properly laid out
        if (targetView.getVisibility() != View.VISIBLE) {
            targetView.setVisibility(View.VISIBLE);
        }

        // Force layout if needed
        if (!targetView.isLaidOut()) {
            targetView.post(() -> showStepInternal(stepIndex));
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

        int padding = (int) activity.getResources().getDimension(R.dimen.spacing_xl);

        RectF hole = new RectF(
                targetRect.left - padding,
                targetRect.top - padding,
                targetRect.right + padding,
                targetRect.bottom + padding
        );

        float radius = activity.getResources().getDisplayMetrics().density * 12f;

        ((DimOverlayView) dimOverlay).setHole(hole, radius);

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

        // Set elevations
        highlightView.setElevation(10f);
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
        nextButton.setText(currentStep == steps.size() - 1 ? "Končaj" : "Naprej");

        prevButton.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            currentStep--;
            showStep(currentStep);
        });

        nextButton.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            if (currentStep == steps.size() - 1) {
                finish();
            } else {
                currentStep++;
                showStep(currentStep);
            }
        });

        skipButton.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            finish();
        });

        overlayContainer.addView(guideLayout);

        // Animate the new guide layout using AnimationHelper
        animateGuideIn(highlightView, textContainer, targetView);
    }

    /**
     * Animate guide elements in using AnimationHelper
     */
    private void animateGuideIn(View highlightView, View textContainer, View targetView) {
        // Highlight pulses in with pop
        AnimationHelper.popIn(highlightView, 400);

        // Text container slides in from bottom
        textContainer.setTranslationY(300f);
        textContainer.setAlpha(0f);
        textContainer.postDelayed(() -> {
            AnimationHelper.fadeIn(textContainer, 300);
            textContainer.animate()
                    .translationY(0f)
                    .setDuration(400)
                    .start();
        }, 200);

        // Wiggle the target view to draw attention
        targetView.postDelayed(() -> {
            AnimationHelper.bounce(targetView);
        }, 100);
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

        highlightView.setElevation(16);
        highlightView.setOutlineProvider(android.view.ViewOutlineProvider.BACKGROUND);
        highlightView.setClipToOutline(false);

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
        int highlightPadding = spacingXl;

        int horizontalMargins = spacingXl * 2;
        textContainer.measure(
                View.MeasureSpec.makeMeasureSpec(screenWidth - horizontalMargins, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int textContainerHeight = textContainer.getMeasuredHeight();

        int spaceBelow = screenHeight - (targetRect.bottom + highlightPadding);
        int spaceAbove = targetRect.top - highlightPadding;

        int minSpaceNeeded = textContainerHeight + spacingLarge;

        params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.topMargin = 0;
        params.bottomMargin = 0;

        if (spaceBelow >= minSpaceNeeded) {
            params.topMargin = targetRect.bottom + highlightPadding + spacingLarge;
        } else if (spaceAbove >= minSpaceNeeded) {
            params.topMargin = targetRect.top - highlightPadding - spacingLarge - textContainerHeight;
        } else {
            if (spaceAbove > spaceBelow) {
                int maxTopMargin = Math.max(spacingXl, targetRect.top - highlightPadding - textContainerHeight - spacingLarge);
                params.topMargin = maxTopMargin;
            } else {
                params.topMargin = targetRect.bottom + highlightPadding + spacingLarge;
            }
        }

        params.leftMargin = spacingXl;
        params.rightMargin = spacingXl;

        textContainer.setLayoutParams(params);
    }

    /**
     * Finish and clean up the guide
     */
    private void finish() {
        if (overlayContainer != null && overlayContainer.getParent() != null) {
            // Fade out animation using AnimationHelper before removing
            AnimationHelper.fadeOut(overlayContainer, 300);
            overlayContainer.postDelayed(() -> {
                if (overlayContainer.getParent() != null) {
                    ((ViewGroup) overlayContainer.getParent()).removeView(overlayContainer);
                }

                if (completeListener != null) {
                    completeListener.onGuideComplete();
                }
            }, 300);
        } else {
            if (completeListener != null) {
                completeListener.onGuideComplete();
            }
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