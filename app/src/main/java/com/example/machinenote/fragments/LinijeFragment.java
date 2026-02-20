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

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.GenericFilter;
import com.example.machinenote.Utility.FilterDialogBuilder;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.LinijeBottomSheetFragment;
import com.example.machinenote.databinding.FragmentLinijeBinding;
import com.example.machinenote.models.Linija;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinijeFragment extends BaseFragment {

    private FragmentLinijeBinding binding;
    private ApiManager apiManager;
    private List<Linija> linijeList;
    private GenericAdapter<Linija> adapter;
    private GenericFilter<Linija> filter;
    private String currentSearchQuery = "";

    public LinijeFragment() {}

    public static LinijeFragment newInstance(Context context) {
        LinijeFragment fragment = new LinijeFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = context.getString(R.string.tag_linije);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLinijeBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = GenericAdapter.create(
                getContext(),
                new ArrayList<>(),
                new GenericAdapter.OnItemClickListener<Linija>() {
                    @Override
                    public void onItemClick(Linija linija) {
                        LinijeBottomSheetFragment bottomSheet =
                                LinijeBottomSheetFragment.newInstance(getContext(), linija);
                        bottomSheet.show(getChildFragmentManager(), "LinijeBottomSheet");
                    }

                    @Override
                    public void onButtonClick(Linija linija) {
                        // Handle button click
                    }
                },
                "Naziv Linije",
                "SAP Koda",
                "Prostor Naziv",
                "Aktivna"
        );

        recyclerView.setAdapter(adapter);

        // Setup search functionality
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

        // Setup filter button
        binding.filterSortToggleBtn.setOnClickListener(v -> showFilterDialog());

        fetchLinije();

        return binding.getRoot();
    }

    private void setupFilter() {
        if (linijeList == null || linijeList.isEmpty()) {
            return;
        }

        // Initialize the filter with callback to update the adapter
        filter = new GenericFilter<>(linijeList, filteredList -> {
            updateRecyclerView(filteredList);
        });

        // Register field extractors for filtering
        filter.addFieldExtractor("aktivna", linija -> linija.getLinija_aktivna() != null ? linija.getLinija_aktivna() : "")
                .addFieldExtractor("prostor_naziv", linija -> linija.getProstor_naziv() != null ? linija.getProstor_naziv() : "")
                .addFieldExtractor("lokacija_naziv", linija -> linija.getLokacija_naziv() != null ? linija.getLokacija_naziv() : "")
                .addFieldExtractor("linija_sap", linija -> linija.getLinija_SAP() != null ? linija.getLinija_SAP() : "")
                .setNameExtractor(linija -> linija.getNaziv_linije() != null ? linija.getNaziv_linije() : ""); // Set naziv as the name field for sorting
    }

    private void showFilterDialog() {
        if (filter == null) {
            Toast.makeText(getContext(), "No data available for filtering", Toast.LENGTH_SHORT).show();
            return;
        }

        List<FilterDialogBuilder.FilterField> fields = Arrays.asList(
                new FilterDialogBuilder.FilterField("aktivna", "Aktivna", filter.getUniqueValuesForField("aktivna")),
                new FilterDialogBuilder.FilterField("prostor_naziv", "Prostor", filter.getUniqueValuesForField("prostor_naziv")),
                new FilterDialogBuilder.FilterField("lokacija_naziv", "Lokacija", filter.getUniqueValuesForField("lokacija_naziv")),
                new FilterDialogBuilder.FilterField("linija_sap", "SAP Koda", filter.getUniqueValuesForField("linija_sap"))
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

        List<Linija> currentData = filter.getCurrentFilteredList();

        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            // Apply search on the already filtered data
            String lowerQuery = currentSearchQuery.toLowerCase();
            List<Linija> searchFiltered = currentData.stream()
                    .filter(linija -> lowerQuery.isEmpty()
                            || (linija.getNaziv_linije() != null && linija.getNaziv_linije().toLowerCase().contains(lowerQuery))
                            || (linija.getLinija_SAP() != null && linija.getLinija_SAP().toLowerCase().contains(lowerQuery))
                            || (String.valueOf(linija.getLinija_id()) != null && String.valueOf(linija.getLinija_id()).toLowerCase().contains(lowerQuery))
                            || (linija.getProstor_naziv() != null && linija.getProstor_naziv().toLowerCase().contains(lowerQuery))
                            || (linija.getLokacija_naziv() != null && linija.getLokacija_naziv().toLowerCase().contains(lowerQuery))
                            || (linija.getLinija_aktivna() != null && linija.getLinija_aktivna().toLowerCase().contains(lowerQuery))
                            || (linija.getFullLocationInfo() != null && linija.getFullLocationInfo().toLowerCase().contains(lowerQuery)))
                    .collect(Collectors.toList());

            updateRecyclerView(searchFiltered);
        } else {
            updateRecyclerView(currentData);
        }
    }

    private void updateRecyclerView(List<Linija> filteredData) {
        if (adapter != null) {
            adapter.updateList(filteredData);
        }
    }

    private void fetchLinije() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            apiManager.fetchLinije(new ApiManager.LinijeCallback() {
                @Override
                public void onSuccess(List<Linija> response) {
                    linijeList = response;
                    adapter.updateList(linijeList);
                    setupFilter(); // Setup filter after data is loaded

                    // Shrani podatke v cache
                    String json = new Gson().toJson(linijeList);
                    sharedPreferencesHelper.putString("LinijeList", json);

                    Toast.makeText(getContext(),
                            "Naloženih " + response.size() + " linij",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "API failure: " + errorMessage);
                    Toast.makeText(getContext(), "Napaka pri nalaganju: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                    loadLinijeFromPrefs(sharedPreferencesHelper);
                }
            });
        } else {
            loadLinijeFromPrefs(sharedPreferencesHelper);
            Toast.makeText(getContext(), "Ni povezave s strežnikom - naloženi lokalni podatki",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLinijeFromPrefs(SharedPreferencesHelper prefs) {
        String json = prefs.getString("LinijeList", null);
        if (json != null) {
            try {
                Type type = new TypeToken<List<Linija>>() {}.getType();
                linijeList = new Gson().fromJson(json, type);
                if (linijeList != null && !linijeList.isEmpty()) {
                    adapter.updateList(linijeList);
                    setupFilter(); // Setup filter after data is loaded
                    Log.i(TAG, "Loaded " + linijeList.size() + " linij from cache");
                } else {
                    Log.e(TAG, "Parsed linijeList is empty or null.");
                    showEmptyState();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing cached data: " + e.getMessage());
                showEmptyState();
            }
        } else {
            Log.e(TAG, "No cached data found.");
            showEmptyState();
        }
    }

    private void showEmptyState() {
        // Show empty state message or hide recycler view
        // You can implement this based on your UI needs
        if (adapter != null) {
            adapter.updateList(new ArrayList<>());
        }
        Toast.makeText(getContext(), "Ni podatkov za prikaz", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }
}