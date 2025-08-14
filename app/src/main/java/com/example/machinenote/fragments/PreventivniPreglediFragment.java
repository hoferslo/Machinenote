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
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentPreventivniPreglediBinding;
import com.example.machinenote.models.Linija;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PreventivniPreglediFragment extends BaseFragment {

    private FragmentPreventivniPreglediBinding binding;
    private ApiManager apiManager;
    private List<Linija> linijeList;
    private GenericAdapter<Linija> adapter;

    public PreventivniPreglediFragment() {}

    public static PreventivniPreglediFragment newInstance(Context context) {
        PreventivniPreglediFragment fragment = new PreventivniPreglediFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = "Linije"; // Ali uporabi context.getString(R.string.tag_linije)
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
                new GenericAdapter.OnItemClickListener<Linija>() {
                    @Override
                    public void onItemClick(Linija linija) {
                        // Navigacija na fragment s preventivnimi pregledi za to linijo
                        MainActivity mainActivity = (MainActivity) requireActivity();
                        PreventivniPreglediOpravilaFragment pregledFragment =
                                PreventivniPreglediOpravilaFragment.newInstance(getContext(), linija);
                        mainActivity.loadFragment(pregledFragment);
                    }

                    @Override
                    public void onButtonClick(Linija linija) {
                        // Hitro dejanje - odpri preventivne preglede
                        MainActivity mainActivity = (MainActivity) requireActivity();
                        PreventivniPreglediOpravilaFragment pregledFragment =
                                PreventivniPreglediOpravilaFragment.newInstance(getContext(), linija);
                        mainActivity.loadFragment(pregledFragment);
                    }
                },
                // Mapiranje polj za GenericAdapter - prilagojeno za Linijo
                "Linija SAP",        // -> linija.getLinija_SAP()
                "Naziv linije",      // -> linija.getNaziv_linije()
                "Lokacija",          // -> linija.getFullLocationInfo()
                "Št. sklopov",       // -> String.valueOf(linija.getStevilo_sklopov())
                "Status",            // -> linija.getLinija_aktivna()
                ""                   // Prazen za zadnje polje
        );

        recyclerView.setAdapter(adapter);

        binding.idOfDuty.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLinije(newText);
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
            }
        });

        fetchLinije();
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

    private void fetchLinije() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            apiManager.fetchLinije(new ApiManager.LinijeCallback() {
                @Override
                public void onSuccess(List<Linija> response) {
                    linijeList = response;
                    adapter.updateList(linijeList);

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
        linijeList = new ArrayList<>();
        adapter.updateList(linijeList);
        Toast.makeText(getContext(), "Ni podatkov za prikaz", Toast.LENGTH_SHORT).show();
    }

    private void filterLinije(String query) {
        if (linijeList != null) {
            List<Linija> filtered;

            if (query == null || query.trim().isEmpty()) {
                filtered = new ArrayList<>(linijeList);
            } else {
                String lowerQuery = query.toLowerCase().trim();
                filtered = linijeList.stream()
                        .filter(l ->
                                (l.getLinija_SAP() != null && l.getLinija_SAP().toLowerCase().contains(lowerQuery)) ||
                                        (l.getNaziv_linije() != null && l.getNaziv_linije().toLowerCase().contains(lowerQuery)) ||
                                        (l.getLokacija_naziv() != null && l.getLokacija_naziv().toLowerCase().contains(lowerQuery)) ||
                                        (l.getProstor_naziv() != null && l.getProstor_naziv().toLowerCase().contains(lowerQuery))
                        )
                        .collect(Collectors.toList());
            }

            adapter.updateList(filtered);
            Log.d(TAG, "Filtered " + filtered.size() + " items from " + linijeList.size());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).binding.toolbarTitle.setText(TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}