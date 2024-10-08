package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentRezervniDeliBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;


public class RezervniDeliFragment extends BaseFragment {

    public String TAG = "Rezervni deli";
    FragmentRezervniDeliBinding binding;
    Context context;

    public RezervniDeliFragment() {
        // Required empty public constructor
    }

    public static RezervniDeliFragment newInstance(Context context) {
        RezervniDeliFragment fragment = new RezervniDeliFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentRezervniDeliBinding.inflate(getLayoutInflater());

        binding.switchTabs.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                // Handle button checked/unchecked
                if (isChecked) {
                    // Respond to the button being checked

                } else {
                    // Respond to the button being unchecked

                }
            }
        });

        return binding.getRoot();
    }



    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}