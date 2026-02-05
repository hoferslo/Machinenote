package com.example.machinenote.Utility;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

public class AnimationHelper {

    /**
     * Wiggle animation - prikazuje view z rotation in scale
     */
    public static void wiggle(View view) {
        // Rotation wiggle
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(
                view, "rotation", 0f, -15f, 15f, -15f, 15f, -10f, 10f, 0f
        );
        rotateAnim.setDuration(600);

        // Scale pulse
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(
                view, "scaleX", 1f, 1.15f, 1f, 1.15f, 1f, 1.1f, 1f
        );
        scaleXAnim.setDuration(600);

        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(
                view, "scaleY", 1f, 1.15f, 1f, 1.15f, 1f, 1.1f, 1f
        );
        scaleYAnim.setDuration(600);

        // Kombiniraj vse
        AnimatorSet set = new AnimatorSet();
        set.playTogether(rotateAnim, scaleXAnim, scaleYAnim);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    /**
     * Pop-in animation - view se pojavi z bounce efektom
     */
    public static void popIn(View view) {
        popIn(view, 400);
    }

    public static void popIn(View view, long duration) {
        view.setScaleX(0f);
        view.setScaleY(0f);

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    /**
     * Pop-in z delay
     */
    public static void popInDelayed(View view, long delay) {
        view.setScaleX(0f);
        view.setScaleY(0f);

        view.postDelayed(() -> {
            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .start();
        }, delay);
    }

    /**
     * Pop-in + wiggle kombinirano
     */
    /**
     * Pop-in + wiggle kombinirano z rdečim flash-om (smooth transition)
     */
    /**
     * Pop-in + wiggle kombinirano z rdečim flash-om (icon tint)
     */
    public static void popInAndWiggle(View view, long delay, int originalColorRes) {
        view.setScaleX(0f);
        view.setScaleY(0f);

        view.postDelayed(() -> {
            int originalColor = view.getContext().getResources().getColor(originalColorRes);

            // Nastavi rdečo tint barvo
            if (view instanceof ImageView) {
                ((ImageView) view).setImageTintList(ColorStateList.valueOf(Color.RED));
            }

            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setInterpolator(new OvershootInterpolator(2f))
                    .withEndAction(() -> {
                        // Wiggle animacija
                        wiggle(view);

                        // Smooth color transition nazaj
                        if (view instanceof ImageView) {
                            ValueAnimator colorAnim = ValueAnimator.ofArgb(Color.RED, originalColor);
                            colorAnim.setDuration(400);
                            colorAnim.addUpdateListener(animator -> {
                                ((ImageView) view).setImageTintList(
                                        ColorStateList.valueOf((int) animator.getAnimatedValue())
                                );
                            });
                            colorAnim.setStartDelay(200);
                            colorAnim.start();
                        }
                    })
                    .start();
        }, delay);
    }
    /**
     * Bounce click - ko uporabnik klikne gumb
     */
    public static void bounceClick(View view) {
        view.animate()
                .scaleX(0.925f)
                .scaleY(0.925f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    public static void bounce(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(200)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    /**
     * Fade in animation
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .start();
    }

    /**
     * Fade out animation
     */
    public static void fadeOut(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .start();
    }

    /**
     * Slide in from right
     */
    public static void slideInFromRight(View view, long duration) {
        view.setTranslationX(view.getWidth());
        view.animate()
                .translationX(0f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Slide in from left
     */
    public static void slideInFromLeft(View view, long duration) {
        view.setTranslationX(-view.getWidth());
        view.animate()
                .translationX(0f)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Shake animation - za error feedback
     */
    public static void shake(View view) {
        ObjectAnimator shakeAnim = ObjectAnimator.ofFloat(
                view, "translationX", 0f, -25f, 25f, -25f, 25f, -15f, 15f, -5f, 5f, 0f
        );
        shakeAnim.setDuration(500);
        shakeAnim.start();
    }

    /**
     * Pulse animation - kontinuirano pulziranje
     */
    public static void pulse(View view) {
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        scaleXAnim.setDuration(1000);
        scaleXAnim.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);
        scaleYAnim.setDuration(1000);
        scaleYAnim.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleXAnim, scaleYAnim);
        set.start();
    }

    /**
     * Rotate 360 animation
     */
    public static void rotate360(View view, long duration) {
        view.animate()
                .rotation(360f)
                .setDuration(duration)
                .withEndAction(() -> view.setRotation(0f))
                .start();
    }
}