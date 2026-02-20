package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.FilterDialogBuilder;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.GenericFilter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentNalogeBinding;
import com.example.machinenote.models.DrobniMateriali;
import com.example.machinenote.models.Naloga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NalogeFragment extends BaseFragment {

    FragmentNalogeBinding binding;
    Context context;
    private ApiManager apiManager;
    private List<Naloga> nalogaList;
    private GenericAdapter<Naloga> adapter;
    private GenericFilter<Naloga> filter;
    private String currentSearchQuery = "";
    public NalogeFragment() {
    }

    public static NalogeFragment newInstance(Context context) {
        NalogeFragment fragment = new NalogeFragment();
        fragment.context = context;
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = context.getString(R.string.tag_naloge);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentNalogeBinding.inflate(getLayoutInflater());

        if (!SharedPreferencesHelper.getInstance(context).getRole().isDodajanjeNalog()) {
            binding.vnosNalogeLl.setVisibility(View.GONE);
        }

        setupRecyclerView();
        setupTabNavigation();
        fetchNaloge();


        return binding.getRoot();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = GenericAdapter.create(
                getContext(),
                new ArrayList<>(),
                new GenericAdapter.OnItemClickListener<Naloga>() {
                    @Override
                    public void onItemClick(Naloga item) {
                        // Open CompletedNalogaFragment for the clicked naloga
                        if (getActivity() instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            CompletedNalogaFragment completedFragment = CompletedNalogaFragment.newInstance(context, item);
                            mainActivity.loadFragment(completedFragment);
                        }
                    }

                    @Override
                    public void onButtonClick(Naloga item) {
                        // Handle button click if needed - you could also open the fragment here
                        // or perform a different action like editing the naloga
                        if (getActivity() instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            CompletedNalogaFragment completedFragment = CompletedNalogaFragment.newInstance(context, item);
                            mainActivity.loadFragment(completedFragment);
                        }
                    }
                },
                "Vzdrzevalec",     // Add the field names you want to display
                "Opis",            // in the order you want them
                "Naloga"           // Remove any fields you don't want to show
        );
        binding.filterSortToggleBtn.setOnClickListener(v -> showFilterDialog());
        recyclerView.setAdapter(adapter);
    }

    private void setupTabNavigation() {
        // Tab navigation click listeners
        binding.tabNalogeBtn.setOnClickListener(v -> {
            // Already on this fragment, do nothing or refresh
            fetchNaloge();
        });

        binding.tabVnosNalogeBtn.setOnClickListener(v -> {
            // Switch to DodajNalogoFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(DodajNalogoFragment.newInstance(context));
            }
        });

        // Cancel button (if it exists in your layout)
        if (binding.cancelBtn != null) {
            binding.cancelBtn.setOnClickListener(v -> {
                // Handle cancel action
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.onBackPressed(); // or navigate to another fragment
                }
            });
        }
    }

    private void fetchNaloge() {
        apiManager.getNaloge(new ApiManager.NalogaCallback() {
            @Override
            public void onSuccess(List<Naloga> response) {
                nalogaList = response;
                List<Naloga> prikazaneNaloge = new ArrayList<>();
                for (Naloga n : nalogaList) {
                    if (n.getIzvedenoBool() == 0) { // če še ni izvedeno
                        prikazaneNaloge.add(n);
                    }
                }
                nalogaList = prikazaneNaloge;
                adapter.updateList(nalogaList);
                setupFilter();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Error: " + errorMessage);
                // Optionally load saved data from SharedPreferences here
            }
        });
    }

    private void setupFilter() {
        if (nalogaList == null || nalogaList.isEmpty()) {
            return;
        }

        // Initialize the filter with callback to update the adapter
        filter = new GenericFilter<>(nalogaList, filteredList -> {
            updateRecyclerView(filteredList);
        });

        // Register field extractors for filtering
        filter.addFieldExtractor("vzdrzevalec", nal -> nal.getVzdrzevalec() != null ? nal.getVzdrzevalec() : "")
                .addFieldExtractor("opis", nal -> nal.getOpis() != null ? nal.getOpis() : "")
                .addFieldExtractor("status", nal -> nal.getNaloga() != null ? nal.getNaloga() : "")
                .addFieldExtractor("datum", nal -> nal.getIzvedeno() != null ? nal.getIzvedeno().toString() : "")
                .setNameExtractor(nal -> nal.getOpis() != null ? nal.getOpis() : "");
    }

    private void updateRecyclerView(List<Naloga> filteredData) {
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
                new FilterDialogBuilder.FilterField("vzdrzevalec", "Vzdrževalec", filter.getUniqueValuesForField("vzdrzevalec"))
        );
        FilterDialogBuilder.showFilterDialog(getContext(), fields, (globalSortOrder, criteria) -> {
            filter.applyFilter(globalSortOrder, criteria);
            applySearchAndFilter();
        });
    }

    private void applySearchAndFilter() {
        if (filter == null) {
            return;
        }

        List<Naloga> currentData = filter.getCurrentFilteredList();

        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            String lowerQuery = currentSearchQuery.toLowerCase();
            List<Naloga> searchFiltered = currentData.stream()
                    .filter(n -> lowerQuery.isEmpty()
                            || (String.valueOf(n.getId()) != null && String.valueOf(n.getId()).toLowerCase().contains(lowerQuery))
                            || (n.getOpis() != null && n.getOpis().toLowerCase().contains(lowerQuery))
                            || (n.getVzdrzevalec() != null && n.getVzdrzevalec().toLowerCase().contains(lowerQuery))
                            || (n.getIzvedeno() != null && n.getIzvedeno().toLowerCase().contains(lowerQuery))
                            || (n.getNaloga() != null && n.getNaloga().toString().toLowerCase().contains(lowerQuery)))
                    .collect(Collectors.toList());

            updateRecyclerView(searchFiltered);
        } else {
            updateRecyclerView(currentData);
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