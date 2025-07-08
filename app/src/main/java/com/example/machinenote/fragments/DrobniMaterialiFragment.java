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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDrobniMaterialiBinding;
import com.example.machinenote.models.DrobniMateriali;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrobniMaterialiFragment extends BaseFragment {

    private FragmentDrobniMaterialiBinding binding;
    private ApiManager apiManager;
    private List<DrobniMateriali> drobniMaterialiList;
    private GenericAdapter<DrobniMateriali> adapter;

    public DrobniMaterialiFragment() {}

    public static DrobniMaterialiFragment newInstance(Context context) {
        DrobniMaterialiFragment fragment = new DrobniMaterialiFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = context.getString(R.string.tag_drobni_materiali);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDrobniMaterialiBinding.inflate(inflater, container, false);

        // Setup RecyclerView
        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GenericAdapter<>(getContext(), new ArrayList<>(), new GenericAdapter.OnItemClickListener<DrobniMateriali>() {
            @Override
            public void onItemClick(DrobniMateriali material) {
                Toast.makeText(getContext(),
                        "Material: " + material.getMaterial() + "\nVrsta: " + material.getVrsta() + "\nVelikost: " + material.getVelikost(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onButtonClick(DrobniMateriali material) {
                Toast.makeText(getContext(),
                        "Gumb za: " + material.getMaterial() + " - " + material.getVrsta(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // Setup SearchView
        binding.idOfDuty.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterDrobniMateriali(newText);
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
            }
        });

        // Setup Tab Navigation
        setupTabNavigation();

        // Initialize stock display
        updateStockDisplay(0);

        fetchDrobniMateriali();

        return binding.getRoot();
    }

    private void setupTabNavigation() {
        binding.switchTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                if (checkedId == R.id.tabRezervniDeliBtn) {
                    FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

                    // Pop current fragment and replace
                    fragmentManager.popBackStack();

                    Fragment rezervniDeliFragment = com.example.machinenote.fragments.RezervniDeliFragment.newInstance(mainActivity);
                    mainActivity.loadFragment(rezervniDeliFragment);
                }
            }
        });
    }

    private void updateStockDisplay(int count) {
        binding.materialStock.setText(String.valueOf(count));
    }

    private void fetchDrobniMateriali() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            apiManager.getDrobniMateriali(new ApiManager.DrobniMaterialiCallback() {
                @Override
                public void onSuccess(List<DrobniMateriali> response) {
                    drobniMaterialiList = response;
                    adapter.updateList(drobniMaterialiList);
                    updateStockDisplay(drobniMaterialiList.size());

                    String json = new Gson().toJson(drobniMaterialiList);
                    sharedPreferencesHelper.putString("DrobniMaterialiList", json);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "API failure: " + errorMessage);
                    loadDrobniMaterialiFromPrefs(sharedPreferencesHelper);
                }
            });
        } else {
            loadDrobniMaterialiFromPrefs(sharedPreferencesHelper);
        }
    }

    private void loadDrobniMaterialiFromPrefs(SharedPreferencesHelper prefs) {
        String json = prefs.getString("DrobniMaterialiList", null);
        if (json != null) {
            Type type = new TypeToken<List<DrobniMateriali>>() {}.getType();
            drobniMaterialiList = new Gson().fromJson(json, type);
            if (drobniMaterialiList != null) {
                adapter.updateList(drobniMaterialiList);
                updateStockDisplay(drobniMaterialiList.size());
            } else {
                Log.e(TAG, "Parsed drobniMaterialiList is null.");
            }
        } else {
            Log.e(TAG, "No cached data found.");
        }
    }

    private void filterDrobniMateriali(String query) {
        if (drobniMaterialiList != null) {
            List<DrobniMateriali> filtered = drobniMaterialiList.stream()
                    .filter(m -> query == null || query.isEmpty()
                            || m.getMaterial().toLowerCase().contains(query.toLowerCase())
                            || m.getVrsta().toLowerCase().contains(query.toLowerCase())
                            || m.getVelikost().toLowerCase().contains(query.toLowerCase())
                            || (m.getKvaliteta() != null && m.getKvaliteta().toLowerCase().contains(query.toLowerCase()))
                            || m.getSkladišče().toLowerCase().contains(query.toLowerCase())
                            || m.getRegal().toLowerCase().contains(query.toLowerCase()))
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