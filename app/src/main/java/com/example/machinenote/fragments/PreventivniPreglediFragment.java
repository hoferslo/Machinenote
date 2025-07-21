package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentPreventivniPreglediBinding;
import com.example.machinenote.models.PreventivniPregled;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PreventivniPreglediFragment extends BaseFragment {

    private FragmentPreventivniPreglediBinding binding;
    private ApiManager apiManager;
    private List<PreventivniPregled> preventivniPreglediList;
    private GenericAdapter<PreventivniPregled> adapter;

    public PreventivniPreglediFragment() {}

    public static PreventivniPreglediFragment newInstance(Context context) {
        PreventivniPreglediFragment fragment = new PreventivniPreglediFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = context.getString(R.string.tag_preventivni_pregledi);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPreventivniPreglediBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = GenericAdapter.create(
                getContext(),
                new ArrayList<>(),
                new GenericAdapter.OnItemClickListener<PreventivniPregled>() {
                    @Override
                    public void onItemClick(PreventivniPregled pregled) {
                        /*
                        MainActivity mainActivity = (MainActivity) requireActivity();
                        PreventivniPreglediOpravilaFragment opravilaFragment =
                                PreventivniPreglediOpravilaFragment.newInstance(getContext(), pregled);
                        mainActivity.loadFragment(opravilaFragment); */
                    }

                    @Override
                    public void onButtonClick(PreventivniPregled pregled) {
                        // Handle button click
                    }
                },
                "Naziv",              // Only show these fields
                "ID",                 // in this order
                "Datum",
                "Status"
                // Don't include other fields you don't want to show
        );

        recyclerView.setAdapter(adapter);

        binding.idOfDuty.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPreventivniPregledi(newText);
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
            }
        });

        fetchPreventivniPregledi();

        setupTabNavigation();

        return binding.getRoot();
    }

    private void setupTabNavigation() {
        binding.switchTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                // Add navigation logic for tabs if needed
                // Example:
                // if (checkedId == R.id.tabOtherBtn) {
                //     FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                //     fragmentManager.popBackStack();
                //     Fragment otherFragment = OtherFragment.newInstance(mainActivity);
                //     mainActivity.loadFragment(otherFragment);
                // }
            }
        });
    }

    private void fetchPreventivniPregledi() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            apiManager.getPreventivniPregledi(new ApiManager.PreventivniPreglediCallback() {
                @Override
                public void onSuccess(List<PreventivniPregled> response) {
                    preventivniPreglediList = response;
                    adapter.updateList(preventivniPreglediList);

                    String json = new Gson().toJson(preventivniPreglediList);
                    sharedPreferencesHelper.putString("PreventivniPreglediList", json);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "API failure: " + errorMessage);
                    loadPreventivniPreglediFromPrefs(sharedPreferencesHelper);
                }
            });
        } else {
            loadPreventivniPreglediFromPrefs(sharedPreferencesHelper);
        }
    }

    private void loadPreventivniPreglediFromPrefs(SharedPreferencesHelper prefs) {
        String json = prefs.getString("PreventivniPreglediList", null);
        if (json != null) {
            Type type = new TypeToken<List<PreventivniPregled>>() {}.getType();
            preventivniPreglediList = new Gson().fromJson(json, type);
            if (preventivniPreglediList != null) {
                adapter.updateList(preventivniPreglediList);
            } else {
                Log.e(TAG, "Parsed preventivniPreglediList is null.");
            }
        } else {
            Log.e(TAG, "No cached data found.");
        }
    }

    private void filterPreventivniPregledi(String query) {
        if (preventivniPreglediList != null) {
            List<PreventivniPregled> filtered = preventivniPreglediList.stream()
                    .filter(p -> query == null || query.isEmpty()
                            || p.getNaziv().toLowerCase().contains(query.toLowerCase())
                            || p.getStatus().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateList(filtered);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).binding.toolbarTitle.setText(TAG);
    }
}