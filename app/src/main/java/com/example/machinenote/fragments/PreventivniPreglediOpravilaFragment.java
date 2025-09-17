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
import androidx.appcompat.app.AlertDialog;
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
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.models.Linija;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PreventivniPreglediOpravilaFragment extends BaseFragment {

    private FragmentPreventivniPreglediBinding binding;
    private ApiManager apiManager;
    private List<PreventivniPregled> preventivniPreglediList;
    private List<PreventivniPregled> allPreventivniPreglediList; // Za filtriranje
    private GenericAdapter<PreventivniPregled> adapter;
    private Linija selectedLinija;

    public PreventivniPreglediOpravilaFragment() {}

    public static PreventivniPreglediOpravilaFragment newInstance(Context context, Linija linija) {
        PreventivniPreglediOpravilaFragment fragment = new PreventivniPreglediOpravilaFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.selectedLinija = linija;
        fragment.TAG = (linija != null ? linija.getLinija_SAP() : "");
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
                        // Tukaj odpri opravilo za ta preventivni pregled
                        showExecutionDialog(pregled);
                    }

                    @Override
                    public void onButtonClick(PreventivniPregled pregled) {
                        // Hitro dejanje - začni pregled
                        showExecutionDialog(pregled);
                    }
                },
                // Mapiranje polj za GenericAdapter
                "Opis",              // -> pregled.getOpis()
                "lokacija",          // -> pregled.getFullLocation()
                "Frekvenca",         // -> pregled.getFrekvenca() + " dni"
                "Trajanje",          // -> pregled.getTrajanjeStdMin() + " min"
                "Status",            // -> pregled.getStatusText()
                "Sklop",
                "Naslednji pregled"  // -> pregled.getNaslenjniPregled()
        );

        recyclerView.setAdapter(adapter);

        // Nastavi naslov z informacijami o liniji
        if (selectedLinija != null) {
            String title = selectedLinija.getLinija_SAP() + " - " + selectedLinija.getNaziv_linije();
            ((MainActivity) requireActivity()).binding.toolbarTitle.setText(title);
        }

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

        // Naloži preventivne preglede za izbrano linijo
        fetchPreventivniPreglediForLinija();

        return binding.getRoot();
    }

    private void fetchPreventivniPreglediForLinija() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection && selectedLinija != null) {
            // API call za preventivne preglede - uporabi obstoječi API
            apiManager.getPreventivniPregledi(new ApiManager.PreventivniPreglediCallback() {
                @Override
                public void onSuccess(List<PreventivniPregled> response) {
                    // Filtriraj preglede za izbrano linijo
                    List<PreventivniPregled> filteredPregledi = new ArrayList<>();
                    for (PreventivniPregled pregled : response) {
                        if (pregled.getLinijaSap() != null &&
                                pregled.getLinijaSap().equals(selectedLinija.getLinija_SAP())) {
                            filteredPregledi.add(pregled);
                        }
                    }

                    preventivniPreglediList = filteredPregledi;
                    allPreventivniPreglediList = new ArrayList<>(filteredPregledi); // Copy for filtering
                    adapter.updateList(preventivniPreglediList);

                    // Shrani podatke v cache z ključem, ki vsebuje linija_sap
                    String cacheKey = "PreventivniPreglediList_" + selectedLinija.getLinija_SAP();
                    String json = new Gson().toJson(preventivniPreglediList);
                    sharedPreferencesHelper.putString(cacheKey, json);

                    Toast.makeText(getContext(),
                            "Naloženih " + filteredPregledi.size() + " pregledov za linijo " + selectedLinija.getLinija_SAP(),
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Successfully loaded " + filteredPregledi.size() + " preventivni pregledi for linija " + selectedLinija.getLinija_SAP() +
                            " (filtered from " + response.size() + " total)");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "API failure: " + errorMessage);
                    Toast.makeText(getContext(), "Napaka pri nalaganju: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                    loadPreventivniPreglediFromPrefs(sharedPreferencesHelper);
                }
            });
        } else {
            if (selectedLinija == null) {
                Log.e(TAG, "Selected linija is null");
                Toast.makeText(getContext(), "Napaka: linija ni izbrana", Toast.LENGTH_LONG).show();
                showEmptyState();
            } else {
                loadPreventivniPreglediFromPrefs(sharedPreferencesHelper);
                Toast.makeText(getContext(), "Ni povezave s strežnikom - naloženi lokalni podatki",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPreventivniPreglediFromPrefs(SharedPreferencesHelper prefs) {
        if (selectedLinija == null) {
            showEmptyState();
            return;
        }

        String cacheKey = "PreventivniPreglediList_" + selectedLinija.getLinija_SAP();
        String json = prefs.getString(cacheKey, null);

        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<PreventivniPregled>>() {}.getType();
                preventivniPreglediList = new Gson().fromJson(json, type);

                if (preventivniPreglediList != null && !preventivniPreglediList.isEmpty()) {
                    allPreventivniPreglediList = new ArrayList<>(preventivniPreglediList);
                    adapter.updateList(preventivniPreglediList);
                    Log.i(TAG, "Loaded " + preventivniPreglediList.size() + " pregledi from cache for linija " + selectedLinija.getLinija_SAP());
                } else {
                    Log.e(TAG, "Parsed preventivniPreglediList is empty or null.");
                    showEmptyState();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing cached data: " + e.getMessage());
                showEmptyState();
            }
        } else {
            Log.e(TAG, "No cached data found for linija " + selectedLinija.getLinija_SAP());
            showEmptyState();
        }
    }

    private void showEmptyState() {
        preventivniPreglediList = new ArrayList<>();
        allPreventivniPreglediList = new ArrayList<>();
        adapter.updateList(preventivniPreglediList);

        String message = selectedLinija != null ?
                "Ni preventivnih pregledov za linijo " + selectedLinija.getLinija_SAP() :
                "Ni podatkov za prikaz";

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showExecutionDialog(PreventivniPregled pregled) {
        if (pregled == null) {
            Toast.makeText(getContext(), "Napaka: pregled ni izbran", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = pregled.getOpis() != null && !pregled.getOpis().isEmpty() ?
                pregled.getOpis() :
                ("Pregled za linijo " + (selectedLinija != null ? selectedLinija.getLinija_SAP() : ""));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Začni pregled")
                .setMessage("Ali želite začeti pregled: " + title + "?")
                .setPositiveButton("Začni", (dialog, which) -> {
                    startPregledExecution(pregled);
                })
                .setNegativeButton("Podrobnosti", (dialog, which) -> {
                    showPregledDetails(pregled);
                })
                .setNeutralButton("Prekliči", null)
                .show();
    }

    private void startPregledExecution(PreventivniPregled pregled) {
        if (pregled == null) {
            Toast.makeText(getContext(), "Napaka: pregled ni izbran", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(),
                "Začenjam pregled: " + (pregled.getOpis() != null ? pregled.getOpis() : "Pregled"),
                Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Starting execution for pregled ID: " + pregled.getId());

        // Primer navigacije na fragment za izvajanje:
        MainActivity mainActivity = (MainActivity) requireActivity();
        PregledExecutionFragment executionFragment =
             PregledExecutionFragment.newInstance(getContext(), pregled, selectedLinija);
         mainActivity.loadFragment(executionFragment);
    }

    private void showPregledDetails(PreventivniPregled pregled) {
        if (pregled == null) {
            Toast.makeText(getContext(), "Napaka: pregled ni izbran", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(pregled.getId()).append("\n\n");

        if (selectedLinija != null) {
            details.append("Linija: ").append(selectedLinija.getLinija_SAP())
                    .append(" - ").append(selectedLinija.getNaziv_linije()).append("\n");
        }

        details.append("Opis: ").append(pregled.getOpis() != null ? pregled.getOpis() : "Ni opisa").append("\n");

        if (pregled.getFullLocation() != null) {
            details.append("Lokacija: ").append(pregled.getFullLocation()).append("\n");
        }

        if (pregled.getTrajanjeStdMin() != 0 ) {
            details.append("Trajanje: ").append(pregled.getTrajanjeStdMin()).append(" min\n");
        }

        if (pregled.getFrekvenca() != 0) {
            details.append("Frekvenca: ").append(pregled.getFrekvenca()).append(" dni\n");
        }

        if (pregled.getDatum() != null) {
            details.append("Zadnji pregled: ").append(pregled.getDatum()).append("\n");
        }

        if (pregled.getNaslenjniPregled() != null) {
            details.append("Naslednji pregled: ").append(pregled.getNaslenjniPregled()).append("\n");
        }

        if (pregled.getStatusText() != null) {
            details.append("Status: ").append(pregled.getStatusText()).append("\n");
        }

        if (pregled.getOpombe() != null && !pregled.getOpombe().isEmpty()) {
            details.append("\nOpombe: ").append(pregled.getOpombe());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Podrobnosti pregleda")
                .setMessage(details.toString())
                .setPositiveButton("V redu", null)
                .setNegativeButton("Začni pregled", (dialog, which) -> {
                    startPregledExecution(pregled);
                })
                .show();
    }

    private void filterPreventivniPregledi(String query) {
        if (allPreventivniPreglediList == null) {
            Log.w(TAG, "allPreventivniPreglediList is null, cannot filter");
            return;
        }

        List<PreventivniPregled> filtered;

        if (query == null || query.trim().isEmpty()) {
            filtered = new ArrayList<>(allPreventivniPreglediList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            filtered = allPreventivniPreglediList.stream()
                    .filter(p -> {
                        return (p.getOpis() != null && p.getOpis().toLowerCase().contains(lowerQuery)) ||
                                (p.getLokacijaNaziv() != null && p.getLokacijaNaziv().toLowerCase().contains(lowerQuery)) ||
                                (p.getProstorNaziv() != null && p.getProstorNaziv().toLowerCase().contains(lowerQuery)) ||
                                (p.getSklopLinije() != null && p.getSklopLinije().toLowerCase().contains(lowerQuery)) ||
                                (p.getStatusText() != null && p.getStatusText().toLowerCase().contains(lowerQuery)) ||
                                (p.getLinijaSap() != null && p.getLinijaSap().toLowerCase().contains(lowerQuery)) ||
                                (p.getLastnost() != null && p.getLastnost().toLowerCase().contains(lowerQuery)) ||
                                (p.getOpombe() != null && p.getOpombe().toLowerCase().contains(lowerQuery));
                    })
                    .collect(Collectors.toList());
        }

        preventivniPreglediList = filtered;
        adapter.updateList(filtered);
        Log.d(TAG, "Filtered " + filtered.size() + " items from " + allPreventivniPreglediList.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedLinija != null) {
            String title = selectedLinija.getLinija_SAP() + " - Preventivni pregledi";
            ((MainActivity) requireActivity()).binding.toolbarTitle.setText(title);
        } else {
            ((MainActivity) requireActivity()).binding.toolbarTitle.setText("Preventivni pregledi");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}