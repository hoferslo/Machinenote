package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentKnjizenjeBinding;
import com.example.machinenote.databinding.FragmentZastojiBinding;

public class ZastojiFragment extends BaseFragment {

    public String TAG = "Zastoji";
    FragmentZastojiBinding binding;
    Context context;

    public ZastojiFragment() {
        // Required empty public constructor
    }


    public static ZastojiFragment newInstance(Context context) {
        ZastojiFragment fragment = new ZastojiFragment();
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
        binding = FragmentZastojiBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}