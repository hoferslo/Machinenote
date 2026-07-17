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
import com.example.machinenote.Utility.FuzzySearchHelper;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.GenericFilter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.NarocilaBottomSheetFragment;
import com.example.machinenote.databinding.FragmentNarocilaBinding;
import com.example.machinenote.models.Naloga;
import com.example.machinenote.models.Narocila;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class NarocilaFragment extends BaseFragment {

    FragmentNarocilaBinding binding;
    Context context;
    private ApiManager apiManager;
    private List<Narocila> narocilaList;
    private GenericAdapter<Narocila> adapter;
    private GenericFilter<Narocila> filter;
    private String currentSearchQuery = "";

    private enum StatusCategory { ACTIVE, DELIVERED }
    private StatusCategory currentCategory = StatusCategory.ACTIVE;

    public NarocilaFragment() {
    }

    public static NarocilaFragment newInstance(Context context) {
        NarocilaFragment fragment = new NarocilaFragment();
        fragment.context = context;
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = context.getString(R.string.tag_narocila);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentNarocilaBinding.inflate(getLayoutInflater());

        // Check if user has permission to add orders
        if (!SharedPreferencesHelper.getInstance(context).getRole().isDodajanjeNarocil()) {
            binding.tabVnosNarocilaBtn.setVisibility(View.GONE);
        }

        setupRecyclerView();
        setupTabNavigation();
        setupStatusToggle();
        fetchNarocila();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = GenericAdapter.create(
                getContext(),
                new ArrayList<>(),
                new GenericAdapter.OnItemClickListener<Narocila>() {
                    @Override
                    public void onItemClick(Narocila item) {
                        handleNarociloClick(item);
                    }

                    @Override
                    public void onButtonClick(Narocila item) {

                    }
                },
                "Naziv",           // Add the field names you want to display
                "Naročnik",        // in the order you want them
                "Lokacija",        // Remove any fields you don't want to show
                "Status"
        );

        binding.filterSortToggleBtn.setOnClickListener(v -> showFilterDialog());
        recyclerView.setAdapter(adapter);
    }

    private void handleNarociloClick(Narocila narocilo) {
        // Check if user has permission to manage orders
        if (SharedPreferencesHelper.getInstance(context).getRole().isUpravljanjeNarocil()) {
            // User has permission - open manage fragment
            openManageFragment(narocilo);
        } else {
            // User doesn't have permission - show delivery confirmation bottom sheet
            showNarocilaBottomSheetFragment(narocilo);
        }
    }

    private void openManageFragment(Narocila narocilo) {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            NarocilaManageFragment managedFragment = NarocilaManageFragment.newInstance(context, narocilo);
            mainActivity.loadFragment(managedFragment);
        }
    }

    private void showNarocilaBottomSheetFragment(Narocila narocilo) {
        NarocilaBottomSheetFragment bottomSheet = NarocilaBottomSheetFragment.newInstance(context, narocilo);

        // Set listener for when delivery is confirmed
        bottomSheet.setOnDeliveryConfirmedListener(new NarocilaBottomSheetFragment.OnDeliveryConfirmedListener() {
            @Override
            public void onDeliveryConfirmed() {
                // Refresh the list after delivery confirmation
                fetchNarocila();
            }
        });

        // Show the bottom sheet
        bottomSheet.show(getParentFragmentManager(), "NarocilaBottomSheetFragment");
    }

    private void setupTabNavigation() {
        // Tab navigation click listeners
        binding.tabNarocilaBtn.setOnClickListener(v -> {
            // Already on this fragment, do nothing or refresh
            fetchNarocila();
        });

        binding.tabVnosNarocilaBtn.setOnClickListener(v -> {
            // Switch to DodajNarociloFragment - only if user has permission
            if (SharedPreferencesHelper.getInstance(context).getRole().isDodajanjeNarocil()) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.loadFragment(NarocilaAddFragment.newInstance(context));
                }
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

    private void setupStatusToggle() {
        binding.statusToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.toggleActiveBtn) {
                currentCategory = StatusCategory.ACTIVE;
            } else if (checkedId == R.id.toggleDeliveredBtn) {
                currentCategory = StatusCategory.DELIVERED;
            }

            applyCategoryFilter();
        });
    }

    private void fetchNarocila() {
        apiManager.getNarocila(new ApiManager.NarocilaCallback() {
            @Override
            public void onSuccess(List<Narocila> response) {
                narocilaList = response;

                // Sort by priority and date
                narocilaList.sort((n1, n2) -> {
                    int priority1 = getStatusPriority(n1.getStatus());
                    int priority2 = getStatusPriority(n2.getStatus());

                    // First sort by priority (higher priority first)
                    if (priority1 != priority2) {
                        return Integer.compare(priority2, priority1);
                    }

                    // If same priority, sort by date (earliest first)
                    return compareDates(n1.getRokZaDobavo(), n2.getRokZaDobavo());
                });

                applyCategoryFilter();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Error: " + errorMessage);
            }
        });
    }

    /**
     * Filtrira glavni seznam glede na izbrano kategorijo (aktivna / dostavljena)
     * in ponovno vzpostavi filter/iskanje nad to podmnožico.
     */
    private void applyCategoryFilter() {
        if (narocilaList == null) return;

        List<Narocila> categoryFiltered = narocilaList.stream()
                .filter(n -> currentCategory == StatusCategory.DELIVERED
                        ? isDeliveredStatus(n.getStatus())
                        : !isDeliveredStatus(n.getStatus()))
                .collect(Collectors.toList());

        adapter.updateList(categoryFiltered);
        setupFilter(categoryFiltered);

        // Če je uporabnik nekaj iskal, ponovno uporabi iskanje na novi podmnožici
        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            applySearchAndFilter();
        }
    }

    /**
     * Ali status šteje kot "zaključen" (dostavljeno / preklicano)
     */
    private boolean isDeliveredStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase().trim();
        return s.equals("dostavljeno") || s.equals("preklicano");
    }

    /**
     * Get priority based on status
     * @param status The status string
     * @return Priority value (higher = more important)
     */
    private int getStatusPriority(String status) {
        if (status == null) return 0;

        String statusLower = status.toLowerCase().trim();
        switch (statusLower) {
            case "novo":
                return 4; // Most important
            case "naroceno":
                return 2; // Third most important
            case "v_obdelavi":
                return 3; // Second most important
            case "dostavljeno":
            case "preklicano":
                return 1; // Least important (done)
            default:
                return 0; // Unknown status
        }
    }

    /**
     * Compare two date strings
     * @param date1 First date string (yyyy-MM-dd format)
     * @param date2 Second date string (yyyy-MM-dd format)
     * @return Comparison result (-1, 0, 1)
     */
    private int compareDates(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return 1; // null dates go to end
            if (date2 == null) return -1;

            Date d1 = sdf.parse(date1);
            Date d2 = sdf.parse(date2);

            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;

            return d1.compareTo(d2); // Earlier dates first

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage());
            // Fallback to string comparison
            return date1.compareTo(date2);
        }
    }

    private void setupFilter(List<Narocila> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            adapter.updateList(new ArrayList<>());
            return;
        }

        // Initialize the filter with callback to update the adapter
        filter = new GenericFilter<>(sourceList, filteredList -> {
            updateRecyclerView(filteredList);
        });

        // Register field extractors for filtering -> zdaj po pravih poljih Narocila
        filter.addFieldExtractor("naziv", n -> n.getNaziv() != null ? n.getNaziv() : "")
                .addFieldExtractor("narocnik", n -> n.getNarocnik() != null ? n.getNarocnik() : "")
                .addFieldExtractor("lokacija", n -> n.getLokacija() != null ? n.getLokacija() : "")
                .addFieldExtractor("status", n -> n.getStatus() != null ? n.getStatus() : "")
                .addFieldExtractor("rok_za_dobavo", n -> n.getRokZaDobavo() != null ? n.getRokZaDobavo() : "")
                .setNameExtractor(n -> n.getNaziv() != null ? n.getNaziv() : "");
    }

    private void updateRecyclerView(List<Narocila> filteredData) {
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
                new FilterDialogBuilder.FilterField("naziv", "Naziv", filter.getUniqueValuesForField("naziv")),
                new FilterDialogBuilder.FilterField("narocnik", "Naročnik", filter.getUniqueValuesForField("narocnik")),
                new FilterDialogBuilder.FilterField("lokacija", "Lokacija", filter.getUniqueValuesForField("lokacija")),
                new FilterDialogBuilder.FilterField("status", "Status", filter.getUniqueValuesForField("status"))
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

        List<Narocila> currentData = filter.getCurrentFilteredList();

        // FuzzySearchHelper samodejno obravnava prazen/null query in vrne celoten seznam
        List<Narocila> searchFiltered = FuzzySearchHelper.search(
                currentData,
                currentSearchQuery,
                n -> n.getId() != 0 ? String.valueOf(n.getId()) : "",
                Narocila::getNaziv,
                Narocila::getNarocnik,
                Narocila::getLokacija,
                Narocila::getStatus,
                Narocila::getRokZaDobavo,
                Narocila::getDatumPredvideneDobave,
                n -> n.getAdminOpomba() != null ? n.getAdminOpomba() : ""
        );

        updateRecyclerView(searchFiltered);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();

        // Refresh data when returning to this fragment
        fetchNarocila();
    }
}