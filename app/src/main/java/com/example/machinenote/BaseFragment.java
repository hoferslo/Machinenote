package com.example.machinenote;

import androidx.fragment.app.Fragment;
import android.app.Activity;
import android.util.Log;

import com.example.machinenote.activities.MainActivity;

public class BaseFragment extends Fragment {
    public String TAG = "BaseFragment";

    @Override
    public void onResume() {
        // CRITICAL FIX: Always check if fragment is attached before calling requireActivity()
        if (!isAdded() || isDetached() || getActivity() == null) {
            Log.w(TAG, "Fragment not attached to activity, skipping onResume");
            return;
        }

        super.onResume();

        try {
            // Use getActivity() instead of requireActivity() for additional safety
            Activity activity = getActivity();
            if (activity instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) activity;
                mainActivity.binding.toolbarTitle.setText(TAG);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage());
        }
    }
}