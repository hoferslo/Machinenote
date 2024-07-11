package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.machinenote.databinding.FragmentKnjizenjeBinding;


public class KnjizenjeFragment extends Fragment {

    FragmentKnjizenjeBinding binding;
    Context context;

    public KnjizenjeFragment() {
        // Required empty public constructor
    }

    public static KnjizenjeFragment newInstance(Context context) {
        KnjizenjeFragment fragment = new KnjizenjeFragment();
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
        binding = FragmentKnjizenjeBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }
}