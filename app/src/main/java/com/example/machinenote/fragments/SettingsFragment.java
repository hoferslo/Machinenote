package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentSettingsBinding;


public class SettingsFragment extends BaseFragment {

    public String TAG = "Settings";
    FragmentSettingsBinding binding;
    Context context;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(Context context) {
        SettingsFragment fragment = new SettingsFragment();
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
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}