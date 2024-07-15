package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentRemontiBinding;
import com.example.machinenote.databinding.FragmentZastojiBinding;


public class RemontiFragment extends BaseFragment {

    public String TAG = "Remonti";
    FragmentRemontiBinding binding;
    Context context;


    public static RemontiFragment newInstance(Context context) {
        RemontiFragment fragment = new RemontiFragment();
        fragment.context = context;
        return fragment;
    }

    public static RemontiFragment newInstance(String param1, String param2) {
        RemontiFragment fragment = new RemontiFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRemontiBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }
}