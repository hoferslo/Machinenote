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
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.Utility.PreventivniPreglediPodsklopAdapter;
import com.example.machinenote.databinding.FragmentPreventivniPreglediBinding;
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.SklopLinije;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PreventivniPreglediOpravilaPodsklopFragment extends BaseFragment {

    private FragmentPreventivniPreglediBinding binding;
    private ApiManager apiManager;
    private List<SklopLinije> podsklopList;
    private List<SklopLinije> allPodsklopList;
    private PreventivniPreglediPodsklopAdapter adapter;
    private Linija selectedLinija;

    public PreventivniPreglediOpravilaPodsklopFragment() {}


    public static PreventivniPreglediOpravilaPodsklopFragment newInstance(Context context, Linija linija) {
        PreventivniPreglediOpravilaPodsklopFragment fragment = new PreventivniPreglediOpravilaPodsklopFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.selectedLinija = linija;

        String tagName = "Podsklopi";

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

    public static boolean linijaHasPodsklopi(List<PreventivniPregled> preglediList, Linija linija) {
        if (preglediList == null || linija == null || linija.getLinija_SAP() == null) {
            return false;
        }

        for (PreventivniPregled pregled : preglediList) {
            if (pregled.getLinijaSap() != null &&
                    pregled.getLinijaSap().equals(linija.getLinija_SAP()) &&
                    pregled.getPodsklopLinijeId() != 0 &&
                    pregled.getPodsklopLinije() != null &&
                    !pregled.getPodsklopLinije().trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPreventivniPreglediBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupToolbar();
        setupSearchView();
        fetchPodsklopiForLinija();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Ustvari nov adapter
        adapter = new PreventivniPreglediPodsklopAdapter(getContext(), new ArrayList<>(), selectedLinija);

        // Nastavi click listener
        adapter.setOnItemClickListener(sklop -> {
            if (sklop instanceof SklopLinije) {
                navigateToOpravila((SklopLinije) sklop);
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
            ((MainActivity) requireActivity()).binding.toolbarTitle.setText("Podsklopi");
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
                filterPodsklopi(newText);
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
            }
        });
    }

    private void fetchPodsklopiForLinija() {
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
        // Filtriraj preglede za izbrano linijo
        List<PreventivniPregled> filteredPregledi = new ArrayList<>();
        for (PreventivniPregled pregled : response) {
            boolean matchesLinija = pregled.getLinijaSap() != null &&
                    pregled.getLinijaSap().equals(selectedLinija.getLinija_SAP());

            if (matchesLinija) {
                filteredPregledi.add(pregled);
            }
        }

        Log.d(TAG, "Found " + filteredPregledi.size() + " pregledi for linija " + selectedLinija.getLinija_SAP());

        // Grupiranje po podsklopu in štetje opravil
        Map<Integer, SklopLinije> podsklopMap = new HashMap<>();
        Map<Integer, Integer> countMap = new HashMap<>();

        for (PreventivniPregled pregled : filteredPregledi) {
            Log.d(TAG, "Pregled ID=" + pregled.getId() +
                    ", podsklopId=" + pregled.getPodsklopLinijeId() +
                    ", podsklopNaziv=" + pregled.getPodsklopLinije());

            // Preveri če ima veljaven podsklop (ne 0 in ne prazen string)
            if (pregled.getPodsklopLinijeId() != 0 &&
                    pregled.getPodsklopLinije() != null &&
                    !pregled.getPodsklopLinije().trim().isEmpty()) {

                int podsklopId = pregled.getPodsklopLinijeId();

                if (!podsklopMap.containsKey(podsklopId)) {
                    SklopLinije sklop = new SklopLinije();
                    sklop.setId(podsklopId);
                    sklop.setSklopLinije(pregled.getPodsklopLinije());
                    sklop.setNazivPodsklopa(pregled.getNazivPodsklopa());
                    podsklopMap.put(podsklopId, sklop);
                    countMap.put(podsklopId, 0);
                }

                countMap.put(podsklopId, countMap.get(podsklopId) + 1);
            }
        }

        Log.d(TAG, "Found " + podsklopMap.size() + " unique podsklopi");

        // Nastavi število opravil na vsak podsklop
        for (Map.Entry<Integer, SklopLinije> entry : podsklopMap.entrySet()) {
            entry.getValue().setOpravilaCount(countMap.get(entry.getKey()));

        }

        podsklopList = new ArrayList<>(podsklopMap.values());
        allPodsklopList = new ArrayList<>(podsklopList);
        adapter.updateList(new ArrayList<>(podsklopList));

        savePodsklopiToCache(podsklopList, sharedPreferencesHelper);

        Log.d(TAG, "Successfully loaded " + podsklopList.size() + " podsklopi");
    }

    private void handleApiFailure(String errorMessage, SharedPreferencesHelper sharedPreferencesHelper) {
        Log.e(TAG, "API failure: " + errorMessage);
        Toast.makeText(getContext(), "Napaka pri nalaganju: " + errorMessage,
                Toast.LENGTH_LONG).show();
        loadPodsklopiFromPrefs(sharedPreferencesHelper);
    }

    private void handleOfflineMode(SharedPreferencesHelper sharedPreferencesHelper) {
        if (selectedLinija == null) {
            Log.e(TAG, "Selected linija is null");
            Toast.makeText(getContext(), "Napaka: linija ni izbrana", Toast.LENGTH_LONG).show();
            showEmptyState();
        } else {
            loadPodsklopiFromPrefs(sharedPreferencesHelper);
            Toast.makeText(getContext(), "Ni povezave s strežnikom - naloženi lokalni podatki",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void savePodsklopiToCache(List<SklopLinije> podsklopi, SharedPreferencesHelper sharedPreferencesHelper) {
        String cacheKey = "PodsklopiList_" + selectedLinija.getLinija_SAP();
        String json = new Gson().toJson(podsklopi);
        sharedPreferencesHelper.putString(cacheKey, json);
    }

    private void loadPodsklopiFromPrefs(SharedPreferencesHelper prefs) {
        if (selectedLinija == null) {
            showEmptyState();
            return;
        }

        String cacheKey = "PodsklopiList_" + selectedLinija.getLinija_SAP();
        String json = prefs.getString(cacheKey, null);

        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<SklopLinije>>() {}.getType();
                podsklopList = new Gson().fromJson(json, type);

                if (podsklopList != null && !podsklopList.isEmpty()) {
                    allPodsklopList = new ArrayList<>(podsklopList);
                    adapter.updateList(new ArrayList<>(podsklopList));
                    Log.i(TAG, "Loaded " + podsklopList.size() + " podsklopi from cache for linija " + selectedLinija.getLinija_SAP());
                } else {
                    Log.e(TAG, "Parsed podsklopList is empty or null.");
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
        podsklopList = new ArrayList<>();
        allPodsklopList = new ArrayList<>();
        adapter.updateList(new ArrayList<>(podsklopList));

        String message = selectedLinija != null ?
                "Ni podsklopov za linijo " + selectedLinija.getLinija_SAP() :
                "Ni podatkov za prikaz";

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToOpravila(SklopLinije sklop) {
        if (sklop == null) {
            Toast.makeText(getContext(), "Napaka: podsklop ni izbran", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Navigating to opravila for podsklop ID: " + sklop.getId());

        // Navigacija na fragment za opravila s podsklopom
        MainActivity mainActivity = (MainActivity) requireActivity();
        PreventivniPreglediOpravilaFragment opravilaFragment =
                PreventivniPreglediOpravilaFragment.newInstanceWithSklop(getContext(), selectedLinija, sklop);
        mainActivity.loadFragment(opravilaFragment);
    }

    private void filterPodsklopi(String query) {
        if (allPodsklopList == null) {
            Log.w(TAG, "allPodsklopList is null, cannot filter");
            return;
        }

        List<SklopLinije> filtered;

        if (query == null || query.trim().isEmpty()) {
            filtered = new ArrayList<>(allPodsklopList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            filtered = allPodsklopList.stream()
                    .filter(s -> matchesFilter(s, lowerQuery))
                    .collect(Collectors.toList());
        }

        podsklopList = filtered;
        adapter.updateList(new ArrayList<>(filtered));
        Log.d(TAG, "Filtered " + filtered.size() + " items from " + allPodsklopList.size());
    }

    private boolean matchesFilter(SklopLinije sklop, String query) {
        return sklop.getSklopLinije() != null &&
                sklop.getSklopLinije().toLowerCase().contains(query);
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