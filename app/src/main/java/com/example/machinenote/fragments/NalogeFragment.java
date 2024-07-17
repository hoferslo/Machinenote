package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentNalogeBinding;


public class NalogeFragment extends BaseFragment {

    public String TAG = "Naloge";
    FragmentNalogeBinding binding;
    Context context;

    public NalogeFragment() {
        // Required empty public constructor
    }

    public static NalogeFragment newInstance(Context context) {
        NalogeFragment fragment = new NalogeFragment();
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
        binding = FragmentNalogeBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}