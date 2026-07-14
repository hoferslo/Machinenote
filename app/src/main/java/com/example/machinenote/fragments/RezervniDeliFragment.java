
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
// Animation was here AnimationHelper.bounceClick(view);
import com.example.machinenote.Utility.FuzzySearchHelper;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.GenericFilter;
import com.example.machinenote.Utility.FilterDialogBuilder;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.RezervniDeliBottomSheetFragment;
import com.example.machinenote.databinding.FragmentRezervniDeliBinding;
import com.example.machinenote.models.RezervniDel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RezervniDeliFragment extends BaseFragment {

    private FragmentRezervniDeliBinding binding;
    private ApiManager apiManager;
    private List<RezervniDel> rezervniDelList;
    private GenericAdapter<RezervniDel> adapter;
    Context context;
    private GenericFilter<RezervniDel> filter;
    private String currentSearchQuery = "";

    public RezervniDeliFragment() {}

    public static RezervniDeliFragment newInstance(Context context) {
        RezervniDeliFragment fragment = new RezervniDeliFragment();
        fragment.context = context;
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = context.getString(R.string.tag_rezervni_deli);
        return fragment;
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRezervniDeliBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = GenericAdapter.create(
                getContext(),
                new ArrayList<>(),
                new GenericAdapter.OnItemClickListener<RezervniDel>() {
                    @Override
                    public void onItemClick(RezervniDel del) {
                        RezervniDeliBottomSheetFragment bottomSheet =
                                RezervniDeliBottomSheetFragment.newInstance(getContext(), del);
                        bottomSheet.show(getChildFragmentManager(), "RezervniDeliBottomSheet");
                    }

                    @Override
                    public void onButtonClick(RezervniDel del) {
                        // Handle button click
                    }
                },
                "Artikel",
                "ID",
                "Skladišče",
                "Dobavitelj"
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

        fetchRezervniDeli();
        setupTabNavigation();

        return binding.getRoot();
    }

    private void setupFilter() {
        if (rezervniDelList == null || rezervniDelList.isEmpty()) {
            return;
        }

        // Initialize the filter with callback to update the adapter
        filter = new GenericFilter<>(rezervniDelList, filteredList -> {
            updateRecyclerView(filteredList);
        });

        // Register field extractors for filtering
        filter.addFieldExtractor("skladisce", del -> del.getSkladišče() != 0 ? String.valueOf(del.getSkladišče()) : "")
                .addFieldExtractor("dobavitelj", del -> del.getDobavitelj() != null ? del.getDobavitelj() : "")
                .addFieldExtractor("artikel", del -> del.getArtikel() != null ? del.getArtikel() : "")
                .setNameExtractor(del -> del.getArtikel() != null ? del.getArtikel() : ""); // Set artikel as the name field for sorting
    }

    private void showFilterDialog() {
        if (filter == null) {
            Toast.makeText(getContext(), "No data available for filtering", Toast.LENGTH_SHORT).show();
            return;
        }

        List<FilterDialogBuilder.FilterField> fields = Arrays.asList(
                new FilterDialogBuilder.FilterField("skladisce", "Skladišče", filter.getUniqueValuesForField("skladisce")),
                new FilterDialogBuilder.FilterField("dobavitelj", "Dobavitelj", filter.getUniqueValuesForField("dobavitelj"))
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

        List<RezervniDel> currentData = filter.getCurrentFilteredList();

        List<RezervniDel> searchFiltered = FuzzySearchHelper.search(
                currentData,
                currentSearchQuery,
                RezervniDel::getArtikel,
                RezervniDel::getArtikel_dolgi_text,
                d -> String.valueOf(d.getId()),
                d -> String.valueOf(d.getSkladišče()),
                RezervniDel::getDobavitelj
        );

        updateRecyclerView(searchFiltered);
    }

    private void updateRecyclerView(List<RezervniDel> filteredData) {
        if (adapter != null) {
            adapter.updateList(filteredData);
        }
    }

    private void setupTabNavigation() {
        binding.tabSmallMaterialsBtn.setOnClickListener(v -> {
            // Animation was here AnimationHelper.bounceClick(v);
                // Switch to DodajNalogoFragment
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.loadFragment(DrobniMaterialiFragment.newInstance(context));
                }
        });

        binding.tabRezervniDeliBtn.setOnClickListener(v -> {
            // Animation was here AnimationHelper.bounceClick(v);
        });
    }

    private void fetchRezervniDeli() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            apiManager.getRezervniDeli(new ApiManager.RezervniDeliCallback() {
                @Override
                public void onSuccess(List<RezervniDel> response) {
                    rezervniDelList = response;
                    adapter.updateList(rezervniDelList);
                    setupFilter(); // Setup filter after data is loaded

                    String json = new Gson().toJson(rezervniDelList);
                    sharedPreferencesHelper.putString("RezervniDelList", json);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "API failure: " + errorMessage);
                    loadRezervniDeliFromPrefs(sharedPreferencesHelper);
                }
            });
        } else {
            loadRezervniDeliFromPrefs(sharedPreferencesHelper);
        }
    }

    private void loadRezervniDeliFromPrefs(SharedPreferencesHelper prefs) {
        String json = prefs.getString("RezervniDelList", null);
        if (json != null) {
            Type type = new TypeToken<List<RezervniDel>>() {}.getType();
            rezervniDelList = new Gson().fromJson(json, type);
            if (rezervniDelList != null) {
                adapter.updateList(rezervniDelList);
                setupFilter(); // Setup filter after data is loaded
            } else {
                Log.e(TAG, "Parsed rezervniDelList is null.");
            }
        } else {
            Log.e(TAG, "No cached data found.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }
}