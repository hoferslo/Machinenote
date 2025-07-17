package com.example.machinenote.Utility;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for creating confetti animations
 * Usage: ConfettiHelper.showConfetti(context, parentViewGroup);
 */
public class ConfettiHelper {

    private List<ImageView> confettiPieces = new ArrayList<>();
    private RelativeLayout confettiContainer;
    private Random random = new Random();
    private Handler confettiHandler = new Handler();
    private Context context;

    // Confetti configuration
    private static final int CONFETTI_COUNT = 80;
    private static final int CONFETTI_DURATION = 6000; // 6 seconds
    private static final int MIN_FALL_DURATION = 3000; // 3 seconds
    private static final int MAX_FALL_DURATION = 5000; // 5 seconds
    private static final int MIN_CONFETTI_SIZE_DP = 8;
    private static final int MAX_CONFETTI_SIZE_DP = 16;

    private static final int[] CONFETTI_COLORS = {
            Color.parseColor("#FF6B6B"), // Red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#45B7D1"), // Blue
            Color.parseColor("#96CEB4"), // Green
            Color.parseColor("#FFEAA7"), // Yellow
            Color.parseColor("#DDA0DD"), // Plum
            Color.parseColor("#98D8C8"), // Mint
            Color.parseColor("#F7DC6F"), // Light Yellow
            Color.parseColor("#BB8FCE"), // Light Purple
            Color.parseColor("#85C1E9")  // Light Blue
    };

    private ConfettiHelper(Context context) {
        this.context = context;
    }

    /**
     * Static method to show confetti animation
     * @param context The context
     * @param parentView The parent ViewGroup where confetti will be displayed
     */
    public static void showConfetti(Context context, ViewGroup parentView) {
        ConfettiHelper helper = new ConfettiHelper(context);
        helper.startConfetti(parentView);
    }

    /**
     * Static method to show confetti animation with custom duration
     * @param context The context
     * @param parentView The parent ViewGroup where confetti will be displayed
     * @param durationMs Duration in milliseconds
     */
    public static void showConfetti(Context context, ViewGroup parentView, int durationMs) {
        ConfettiHelper helper = new ConfettiHelper(context);
        helper.startConfetti(parentView, durationMs);
    }

    private void startConfetti(ViewGroup parentView) {
        startConfetti(parentView, CONFETTI_DURATION);
    }

    private void startConfetti(ViewGroup parentView, int duration) {
        setupConfettiContainer(parentView);
        createConfettiPieces();

        // Remove confetti after specified duration
        confettiHandler.postDelayed(() -> {
            cleanup();
        }, duration);
    }

    private void setupConfettiContainer(ViewGroup parentView) {
        // Create a RelativeLayout to hold confetti pieces
        confettiContainer = new RelativeLayout(context);
        confettiContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        // Add confetti container to the parent view
        parentView.addView(confettiContainer);
    }

    private void createConfettiPieces() {
        // Clear any existing confetti
        confettiContainer.removeAllViews();
        confettiPieces.clear();

        // Create confetti pieces in waves for better effect
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            // Delay each piece slightly for a more natural burst effect
            confettiHandler.postDelayed(() -> createSingleConfettiPiece(), i * 50);
        }
    }

    private void createSingleConfettiPiece() {
        ImageView confetti = new ImageView(context);

        // Set confetti appearance
        confetti.setBackgroundColor(getRandomColor());

        // Random size between MIN_CONFETTI_SIZE_DP and MAX_CONFETTI_SIZE_DP
        float density = context.getResources().getDisplayMetrics().density;
        int minSize = (int) (MIN_CONFETTI_SIZE_DP * density);
        int maxSize = (int) (MAX_CONFETTI_SIZE_DP * density);
        int size = minSize + random.nextInt(maxSize - minSize);

        // Get screen dimensions
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        // Randomly choose left or right side
        boolean fromLeft = random.nextBoolean();

        // Set initial position at the sides of the screen
        float initialX, initialY;

        if (fromLeft) {
            initialX = -size; // Start off-screen left
        } else {
            initialX = screenWidth; // Start off-screen right
        }

        // Start from middle vertical area of screen
        initialY = screenHeight * 0.3f + random.nextFloat() * (screenHeight * 0.4f);

        // Create layout params but don't set margins - we'll use setX/setY instead
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        confetti.setLayoutParams(params);

        // Set initial position using setX and setY
        confetti.setX(initialX);
        confetti.setY(initialY);
        confetti.setRotation(random.nextFloat() * 360);

        confettiContainer.addView(confetti);
        confettiPieces.add(confetti);

        // Animate the confetti piece
        animateConfettiPiece(confetti, fromLeft, initialX, initialY);
    }

    private void animateConfettiPiece(ImageView confetti, boolean fromLeft, float initialX, float initialY) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        // Random duration for variation
        int animationDuration = MIN_FALL_DURATION + random.nextInt(MAX_FALL_DURATION - MIN_FALL_DURATION);

        // Create burst effect - shoot out from sides then fall
        ValueAnimator burstAnimator = ValueAnimator.ofFloat(0f, 1f);
        burstAnimator.setDuration(animationDuration);
        burstAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // Calculate target positions for the burst
        float burstTargetX, finalTargetY;

        if (fromLeft) {
            // Burst from left side towards center-right
            burstTargetX = screenWidth * (0.4f + random.nextFloat() * 0.6f); // 40-100% of screen width
        } else {
            // Burst from right side towards center-left
            burstTargetX = screenWidth * (random.nextFloat() * 0.6f); // 0-60% of screen width
        }

        // Final target is bottom of screen
        finalTargetY = screenHeight + 100;

        burstAnimator.addUpdateListener(animation -> {
            float progress = animation.getAnimatedFraction();

            // Split animation into two phases
            float burstPhase = Math.min(progress * 2.5f, 1f); // First 40% is burst
            float fallPhase = Math.max((progress - 0.4f) * 1.67f, 0f); // Last 60% is fall

            float currentX, currentY;

            if (progress <= 0.4f) {
                // Burst phase - move from side to center with upward arc
                currentX = initialX + (burstTargetX - initialX) * burstPhase;

                // Create upward arc during burst
                float arcHeight = -150 * (float) Math.sin(burstPhase * Math.PI);
                currentY = initialY + arcHeight;
            } else {
                // Fall phase - fall down from burst position
                currentX = burstTargetX;

                // Add some horizontal drift during fall
                float drift = (float) Math.sin(progress * 8) * 20;
                currentX += drift;

                // Fall down
                float fallDistance = (finalTargetY - initialY) * fallPhase;
                currentY = initialY + fallDistance;
            }

            confetti.setX(currentX);
            confetti.setY(currentY);

            // Add rotation
            confetti.setRotation(confetti.getRotation() + 6);

            // Fade out near the end
            if (progress > 0.8f) {
                float alpha = 1f - ((progress - 0.8f) / 0.2f);
                confetti.setAlpha(alpha);
            }
        });

        burstAnimator.start();
    }

    private int getRandomColor() {
        return CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.length)];
    }

    private void cleanup() {
        if (confettiContainer != null && confettiContainer.getParent() != null) {
            ViewGroup parent = (ViewGroup) confettiContainer.getParent();
            parent.removeView(confettiContainer);
        }

        if (confettiPieces != null) {
            confettiPieces.clear();
        }

        if (confettiHandler != null) {
            confettiHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Method to create confetti with custom colors
     * @param context The context
     * @param parentView The parent ViewGroup
     * @param customColors Array of custom colors (hex format)
     */
    public static void showConfettiWithCustomColors(Context context, ViewGroup parentView, int[] customColors) {
        ConfettiHelper helper = new ConfettiHelper(context);
        // Store original colors
        int[] originalColors = CONFETTI_COLORS.clone();

        // Replace with custom colors temporarily
        System.arraycopy(customColors, 0, CONFETTI_COLORS, 0, Math.min(customColors.length, CONFETTI_COLORS.length));

        helper.startConfetti(parentView);

        // Restore original colors
        System.arraycopy(originalColors, 0, CONFETTI_COLORS, 0, CONFETTI_COLORS.length);
    }
}