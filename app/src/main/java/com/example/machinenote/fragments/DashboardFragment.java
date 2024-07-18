package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDashboardBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends BaseFragment {

    public String TAG = "Glavna stran";
    FragmentDashboardBinding binding;
    Context context;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance(Context context) {
        DashboardFragment fragment = new DashboardFragment();
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

        binding = FragmentDashboardBinding.inflate(getLayoutInflater());

        binding.knjizenje.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(KnjizenjeFragment.newInstance(context));
        });

        binding.zastoji.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(ZastojiFragment.newInstance(context));
        });

        binding.remonti.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(RemontiFragment.newInstance(context));
        });

        binding.imenik.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(ImenikFragment.newInstance(context));
        });

        binding.naloge.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(NalogeFragment.newInstance(context));
        });

        binding.orodja.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(OrodjaFragment.newInstance(context));
        });

        binding.rezervniDeli.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(RezervniDeliFragment.newInstance(context));
        });
        binding.register.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(RegisterFragment.newInstance(context));
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showDrawerIcon();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }
}