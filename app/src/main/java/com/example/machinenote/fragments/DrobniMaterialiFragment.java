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
import com.example.machinenote.Utility.AnimationHelper;
import com.example.machinenote.Utility.FilterDialogBuilder;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.GenericFilter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.DrobniMaterialiBottomSheetFragment;
import com.example.machinenote.customFragments.RezervniDeliBottomSheetFragment;
import com.example.machinenote.databinding.FragmentDrobniMaterialiBinding;
import com.example.machinenote.models.DrobniMateriali;
import com.example.machinenote.models.RezervniDel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DrobniMaterialiFragment extends BaseFragment {

    private FragmentDrobniMaterialiBinding binding;
    private ApiManager apiManager;
    private List<DrobniMateriali> drobniMaterialiList;
    private GenericFilter<DrobniMateriali> filter;
    Context context;
    private GenericAdapter<DrobniMateriali> adapter;
    private String currentSearchQuery = "";

    public DrobniMaterialiFragment() {}

    public static DrobniMaterialiFragment newInstance(Context context) {
        DrobniMaterialiFragment fragment = new DrobniMaterialiFragment();
        fragment.context = context;
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

        adapter = GenericAdapter.create(
                getContext(),
                new ArrayList<>(),
                new GenericAdapter.OnItemClickListener<DrobniMateriali>() {
                    @Override
                    public void onItemClick(DrobniMateriali del) {
                        DrobniMaterialiBottomSheetFragment bottomSheet =
                                DrobniMaterialiBottomSheetFragment.newInstance(getContext(), del);
                        bottomSheet.show(getChildFragmentManager(), "DrobniMaterialiBottomSheet");
                    }

                    @Override
                    public void onButtonClick(DrobniMateriali del) {
                        // Handle button click
                    }
                },
                "ID",              // Only show these fields
                "Material",   // in this order
                "Vrsta",
                "Velikost",
                "Skladišče",
                "Regal"
                // Don't include "dolgi_opis" or any other fields you don't want
        );

        recyclerView.setAdapter(adapter);

        // Setup SearchView
        binding.idOfDuty.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applySearchAndFilter();
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
            }
        });

        binding.filterSortToggleBtn.setOnClickListener(v -> showFilterDialog());

        // Setup Tab Navigation
        setupTabNavigation();

        fetchDrobniMateriali();

        return binding.getRoot();
    }

    private void setupTabNavigation() {
        binding.tabRezervniDeliBtn.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                fragmentManager.popBackStack();
                Fragment RezervniDeliFragment = com.example.machinenote.fragments.RezervniDeliFragment.newInstance(mainActivity);
                mainActivity.loadFragment(RezervniDeliFragment);
            }
        });

        binding.tabSmallMaterialsBtn.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
        });
    }
    private void fetchDrobniMateriali() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            apiManager.getDrobniMateriali(new ApiManager.DrobniMaterialiCallback() {
                @Override
                public void onSuccess(List<DrobniMateriali> response) {
                    Log.d(TAG, "API Success - received " + response.size() + " items");
                    for (DrobniMateriali item : response) {
                        Log.d(TAG, "Item: " + item.getMaterial() + " - " + item.getVrsta());
                    }
                    drobniMaterialiList = response;
                    adapter.updateList(drobniMaterialiList);
                    String json = new Gson().toJson(drobniMaterialiList);
                    sharedPreferencesHelper.putString("DrobniMaterialiList", json);
                    setupFilter();
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
            } else {
                Log.e(TAG, "Parsed drobniMaterialiList is null.");
            }
        } else {
            Log.e(TAG, "No cached data found.");
        }
    }

    private void setupFilter() {
        if (drobniMaterialiList == null || drobniMaterialiList.isEmpty()) {
            return;
        }

        // Initialize the filter with callback to update the adapter
        filter = new GenericFilter<>(drobniMaterialiList, filteredList -> {
            updateRecyclerView(filteredList);
        });

        // Register field extractors for filtering
        filter.addFieldExtractor("skladisce", del -> del.getSkladišče() != 0 ? String.valueOf(del.getSkladišče()) : "")
                .addFieldExtractor("material", del -> del.getMaterial() != null ? del.getMaterial() : "")
                .addFieldExtractor("vrsta", del -> del.getVrsta() != null ? del.getVrsta() : "")
                .addFieldExtractor("velikost", del -> del.getVelikost() != null ? del.getVelikost() : "")
                .setNameExtractor(del -> del.getVrsta() != null ? del.getVrsta() : ""); // Set artikel as the name field for sorting
    }

    private void updateRecyclerView(List<DrobniMateriali> filteredData) {
        if (adapter != null) {
            adapter.updateList(filteredData);
        }
    }

    private void showFilterDialog() {
        if (filter == null) {
            Toast.makeText(getContext(), "No data available for filtering", Toast.LENGTH_SHORT).show();
            return;
        }

        List<FilterDialogBuilder.FilterField> fields = Arrays.asList(
                new FilterDialogBuilder.FilterField("velikost", "Velikost", filter.getUniqueValuesForField("velikost")),
                new FilterDialogBuilder.FilterField("material", "Material", filter.getUniqueValuesForField("material")),
                new FilterDialogBuilder.FilterField("skladisce", "Skladišče", filter.getUniqueValuesForField("skladisce")),
                new FilterDialogBuilder.FilterField("kvaliteta", "Kvaliteta", filter.getUniqueValuesForField("kvaliteta"))
        );
        FilterDialogBuilder.showFilterDialog(getContext(), fields, (globalSortOrder, criteria) -> {
            filter.applyFilter(globalSortOrder, criteria);
            // Also apply search if there's an active search query
            applySearchAndFilter();
        });
    }

    private void applySearchAndFilter() {
        if (filter == null) {
            return;
        }

        List<DrobniMateriali> currentData = filter.getCurrentFilteredList();

        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            // Apply search on the already filtered data
            String lowerQuery = currentSearchQuery.toLowerCase();
            List<DrobniMateriali> searchFiltered = currentData.stream()
                    .filter(d -> lowerQuery.isEmpty()
                            || (String.valueOf(d.getID()) != null && String.valueOf(d.getID()).toLowerCase().contains(lowerQuery))
                            || (d.getVrsta() != null && d.getVrsta().toLowerCase().contains(lowerQuery))
                            || (String.valueOf(d.getID()) != null && String.valueOf(d.getID()).toLowerCase().contains(lowerQuery))
                            || (d.getMaterial() != null && d.getMaterial().toLowerCase().contains(lowerQuery))
                            || (d.getVelikost() != null && d.getVelikost().toLowerCase().contains(lowerQuery))
                            || (String.valueOf(d.getSkladišče()) != null && String.valueOf(d.getSkladišče()).toLowerCase().contains(lowerQuery))
                            || (d.getKvaliteta() != null && d.getKvaliteta().toLowerCase().contains(lowerQuery)))
                    .collect(Collectors.toList());

            updateRecyclerView(searchFiltered);
        } else {
            updateRecyclerView(currentData);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).binding.toolbarTitle.setText(TAG);
    }
}