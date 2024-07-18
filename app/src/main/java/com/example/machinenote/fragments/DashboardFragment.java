package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDashboardBinding;
import com.example.machinenote.models.Role;

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

        binding.knjizenjeDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(KnjizenjeFragment.newInstance(context));
        });

        binding.zastojiDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(ZastojiFragment.newInstance(context));
        });

        binding.remontiDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(RemontiFragment.newInstance(context));
        });

        binding.imenikDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(ImenikFragment.newInstance(context));
        });

        binding.nalogeDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(NalogeFragment.newInstance(context));
        });

        binding.orodjaDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(OrodjaFragment.newInstance(context));
        });

        binding.rezervniDeliDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(RezervniDeliFragment.newInstance(context));
        });
        binding.registerDashBtn.setOnClickListener(view -> {

            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.loadFragment(RegisterFragment.newInstance(context));
        });

        SharedPreferencesHelper sharedPreferencesHelper =  SharedPreferencesHelper.getInstance(context);
        Role role = sharedPreferencesHelper.getRole();
        initRoleLogic(role);

        return binding.getRoot();
    }

    public void initRoleLogic(Role role) {
        binding.knjizenjeDashBtn.setVisibility(getVisibility(role.isKnjizenje()));
        binding.imenikDashBtn.setVisibility(getVisibility(role.isImenik()));
        binding.nalogeDashBtn.setVisibility(getVisibility(role.isNaloge()));
        binding.orodjaDashBtn.setVisibility(getVisibility(role.isOrodja()));
        binding.registerDashBtn.setVisibility(getVisibility(role.isRegister()));
        binding.remontiDashBtn.setVisibility(getVisibility(role.isRemonti()));
        binding.rezervniDeliDashBtn.setVisibility(getVisibility(role.isRezervniDeli()));
        binding.zastojiDashBtn.setVisibility(getVisibility(role.isZastoji()));
        binding.preventivniPreglediDashBtn.setVisibility(getVisibility(role.isPreventivniPregledi()));



    }

    public int getVisibility(boolean state) {
        if (state) { return View.VISIBLE; } else { return View.GONE; }
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