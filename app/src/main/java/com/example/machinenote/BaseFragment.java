package com.example.machinenote;

import androidx.fragment.app.Fragment;

import com.example.machinenote.activities.MainActivity;

public class BaseFragment  extends Fragment {
    public String TAG = "BaseFragment";


    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}
