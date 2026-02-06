package com.example.machinenote;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.machinenote.Utility.AnimationHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register fragment lifecycle callback
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f,
                                                      @NonNull View v, @Nullable Bundle savedInstanceState) {
                        applyBounceToTargets(v);
                    }
                }, true
        );
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Apply to activity-level views (toolbar, etc.)
        getWindow().getDecorView().post(() -> {
            View root = getWindow().getDecorView().getRootView();
            applyBounceToTargets(root);
        });
    }

    private void applyBounceToTargets(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyBounceToTargets(group.getChildAt(i));
            }
        }

        if (view instanceof Button || view instanceof ImageButton || view instanceof Spinner) {
            // Skip help button or any button with a specific ID
            if (view.getId() == R.id.helpButton) {
                return;
            }
            attachBounceTouch(view);
        }
    }

    private void attachBounceTouch(View target) {
        target.setOnTouchListener((v, event) -> {
            if (!v.isEnabled()) return false;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    AnimationHelper.bounceClick(v);
                    break;

                case MotionEvent.ACTION_UP:
                    // Reset scale to normal
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    break;

                case MotionEvent.ACTION_CANCEL:
                    v.animate().cancel();
                    v.setScaleX(1f);
                    v.setScaleY(1f);
                    break;
            }
            return false;
        });
    }
}