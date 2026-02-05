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
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.Utility.PreventivniPreglediAdapter;
import com.example.machinenote.databinding.FragmentPreventivniPreglediBinding;
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.SklopLinije;
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
    private PreventivniPreglediAdapter adapter;
    private Linija selectedLinija;
    private SklopLinije selectedSklop;

    public PreventivniPreglediOpravilaFragment() {}

    public static PreventivniPreglediOpravilaFragment newInstance(Context context, Linija linija) {
        PreventivniPreglediOpravilaFragment fragment = new PreventivniPreglediOpravilaFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.selectedLinija = linija;

        String tagName = "PreglediOpravila";

        if (linija != null && linija.getLinija_SAP() != null && !linija.getLinija_SAP().isEmpty()) {
            String[] parts = linija.getLinija_SAP().replace("_", " ").trim().split("\\s+");

            if (parts.length >= 2) {
                tagName = parts[0] + " " + parts[1];
            } else if (parts.length == 1) {
                tagName = parts[0];
            }

            if (tagName.length() > 23) {
                tagName = tagName.substring(0, 23);
            }
        }

        fragment.TAG = tagName;
        return fragment;
    }

    public static PreventivniPreglediOpravilaFragment newInstanceWithSklop(Context context, Linija linija, SklopLinije sklop) {
        PreventivniPreglediOpravilaFragment fragment = new PreventivniPreglediOpravilaFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.selectedLinija = linija;
        fragment.selectedSklop = sklop;

        String tagName = sklop.getSklopLinije();
        if (tagName.length() > 23) {
            tagName = tagName.substring(0, 23);
        }

        fragment.TAG = tagName;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPreventivniPreglediBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupToolbar();
        setupSearchView();
        fetchPreventivniPreglediForLinija();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Ustvari nov adapter
        adapter = new PreventivniPreglediAdapter(getContext(), new ArrayList<>());

        // Nastavi click listener-je
        adapter.setOnItemClickListener(new PreventivniPreglediAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                if (item instanceof PreventivniPregled) {
                    showExecutionDialog((PreventivniPregled) item);
                }
            }

            @Override
            public void onButtonClick(Object item) {
                if (item instanceof PreventivniPregled) {
                    showExecutionDialog((PreventivniPregled) item);
                }
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar() {
        // Nastavi naslov z informacijami o liniji
        if (selectedLinija != null) {
            String title = TAG;
            ((MainActivity) requireActivity()).binding.toolbarTitle.setText(title);
        } else {
            ((MainActivity) requireActivity()).binding.toolbarTitle.setText("Preventivni pregledi");
        }
    }

    private void setupSearchView() {
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
    }

    private void fetchPreventivniPreglediForLinija() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection && selectedLinija != null) {
            // API call za preventivne preglede
            apiManager.getPreventivniPregledi(new ApiManager.PreventivniPreglediCallback() {
                @Override
                public void onSuccess(List<PreventivniPregled> response) {
                    handleApiSuccess(response, sharedPreferencesHelper);
                }

                @Override
                public void onFailure(String errorMessage) {
                    handleApiFailure(errorMessage, sharedPreferencesHelper);
                }
            });
        } else {
            handleOfflineMode(sharedPreferencesHelper);
        }
    }

    private void handleApiSuccess(List<PreventivniPregled> response, SharedPreferencesHelper sharedPreferencesHelper) {
        List<PreventivniPregled> filteredPregledi = new ArrayList<>();
        for (PreventivniPregled pregled : response) {
            boolean matchesLinija = pregled.getLinijaSap() != null &&
                    pregled.getLinijaSap().equals(selectedLinija.getLinija_SAP());

            // Če imamo selectedSklop, filtriraj tudi po podsklop_linije_id
            boolean matchesSklop = selectedSklop == null ||
                    (pregled.getPodsklopLinije() != null &&
                            pregled.getPodsklopLinijeId() == selectedSklop.getId());

            if (matchesLinija && matchesSklop) {
                filteredPregledi.add(pregled);
            }
        }

        preventivniPreglediList = filteredPregledi;
        allPreventivniPreglediList = new ArrayList<>(filteredPregledi);
        adapter.updateList(new ArrayList<Object>(preventivniPreglediList));

        savePregledToCache(filteredPregledi, sharedPreferencesHelper);

        String message = selectedSklop != null ?
                "Naloženih " + filteredPregledi.size() + " opravil za sklop " + selectedSklop.getSklopLinije() :
                "Naloženih " + filteredPregledi.size() + " pregledov za linijo " + selectedLinija.getLinija_SAP();


        Log.d(TAG, "Successfully loaded " + filteredPregledi.size() + " preventivni pregledi");
    }

    private void handleApiFailure(String errorMessage, SharedPreferencesHelper sharedPreferencesHelper) {
        Log.e(TAG, "API failure: " + errorMessage);
        Toast.makeText(getContext(), "Napaka pri nalaganju: " + errorMessage,
                Toast.LENGTH_LONG).show();
        loadPreventivniPreglediFromPrefs(sharedPreferencesHelper);
    }

    private void handleOfflineMode(SharedPreferencesHelper sharedPreferencesHelper) {
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

    private void savePregledToCache(List<PreventivniPregled> pregledi, SharedPreferencesHelper sharedPreferencesHelper) {
        String cacheKey = "PreventivniPreglediList_" + selectedLinija.getLinija_SAP();
        String json = new Gson().toJson(pregledi);
        sharedPreferencesHelper.putString(cacheKey, json);
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
                    adapter.updateList(new ArrayList<Object>(preventivniPreglediList));
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
        adapter.updateList(new ArrayList<Object>(preventivniPreglediList));

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

        Log.d(TAG, "Starting execution for pregled ID: " + pregled.getId());

        // Navigacija na fragment za izvajanje
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

        StringBuilder details = buildDetailsString(pregled);

        new AlertDialog.Builder(requireContext())
                .setTitle("Podrobnosti pregleda")
                .setMessage(details.toString())
                .setPositiveButton("V redu", null)
                .setNegativeButton("Začni pregled", (dialog, which) -> {
                    startPregledExecution(pregled);
                })
                .show();
    }

    private StringBuilder buildDetailsString(PreventivniPregled pregled) {
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(pregled.getId()).append("\n\n");

        if (selectedLinija != null) {
            details.append("Linija: ").append(selectedLinija.getLinija_SAP())
                    .append(" - ").append(selectedLinija.getNaziv_linije()).append("\n");
        }

        if (pregled.getPodsklopLinije() != null && !pregled.getPodsklopLinije().isEmpty()) {
            details.append("Podsklop: ").append(pregled.getNazivPodsklopa()).append("\n");
        }

        details.append("Opis: ").append(pregled.getOpis() != null ? pregled.getOpis() : "Ni opisa").append("\n");

        if (pregled.getFullLocation() != null) {
            details.append("Lokacija: ").append(pregled.getFullLocation()).append("\n");
        }

        if (pregled.getTrajanjeStdMin() != 0) {
            details.append("Trajanje: ").append(pregled.getTrajanjeStdMin()).append(" min\n");
        }

        if (pregled.getFrekvenca() != 0) {
            details.append("Frekvenca: ").append(pregled.getFrekvenca()).append(" dni\n");
        }

        if (pregled.getDatum() != null) {
            details.append("Zadnji pregled: ").append(pregled.getDatum()).append("\n");
        }

        if (pregled.getNaslednjniPregled() != null) {
            details.append("Naslednji pregled: ").append(pregled.getNaslednjniPregled()).append("\n");
        }

        if (pregled.getStatusText() != null) {
            details.append("Status: ").append(pregled.getStatusText()).append("\n");
        }

        if (pregled.getOpombe() != null && !pregled.getOpombe().isEmpty()) {
            details.append("\nOpombe: ").append(pregled.getOpombe());
        }

        return details;
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
                    .filter(p -> matchesFilter(p, lowerQuery))
                    .collect(Collectors.toList());
        }

        preventivniPreglediList = filtered;
        adapter.updateList(new ArrayList<Object>(filtered));
        Log.d(TAG, "Filtered " + filtered.size() + " items from " + allPreventivniPreglediList.size());
    }

    private boolean matchesFilter(PreventivniPregled pregled, String query) {
        return (pregled.getOpis() != null && pregled.getOpis().toLowerCase().contains(query)) ||
                (pregled.getLokacijaNaziv() != null && pregled.getLokacijaNaziv().toLowerCase().contains(query)) ||
                (pregled.getProstorNaziv() != null && pregled.getProstorNaziv().toLowerCase().contains(query)) ||
                (pregled.getSklopLinije() != null && pregled.getSklopLinije().toLowerCase().contains(query)) ||
                (pregled.getStatusText() != null && pregled.getStatusText().toLowerCase().contains(query)) ||
                (pregled.getLinijaSap() != null && pregled.getLinijaSap().toLowerCase().contains(query)) ||
                (pregled.getLastnost() != null && pregled.getLastnost().toLowerCase().contains(query)) ||
                (pregled.getOpombe() != null && pregled.getOpombe().toLowerCase().contains(query));
    }

    @Override
    public void onResume() {
        super.onResume();
        setupToolbar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}