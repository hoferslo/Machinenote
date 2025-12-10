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
import com.example.machinenote.Utility.PreventivniPreglediAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentPreventivniPreglediBinding;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.fragments.PreventivniPreglediOpravilaFragment;
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
    private List<Linija> allLinijeList; // Za filtriranje
    private PreventivniPreglediAdapter adapter;
    private List<PreventivniPregled> allPreventivniPregledi; // Za preverjanje podsklopov

    public PreventivniPreglediFragment() {}

    public static PreventivniPreglediFragment newInstance(Context context) {
        PreventivniPreglediFragment fragment = new PreventivniPreglediFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = "Preventivni pregledi";
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPreventivniPreglediBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupSearchView();
        fetchLinije();
        fetchPreventivniPregledi(); // Naloži preventivne preglede za preverjanje podsklopov

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Uporabi novi adapter
        adapter = new PreventivniPreglediAdapter(getContext(), new ArrayList<>());

        adapter.setOnItemClickListener(new PreventivniPreglediAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object item) {
                if (item instanceof Linija) {
                    handleLinijaClick((Linija) item);
                }
            }

            @Override
            public void onButtonClick(Object item) {
                if (item instanceof Linija) {
                    handleLinijaClick((Linija) item);
                }
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void handleLinijaClick(Linija linija) {
        MainActivity mainActivity = (MainActivity) requireActivity();

        // Preveri če ima linija podsklope
        if (allPreventivniPregledi != null &&
                PreventivniPreglediOpravilaPodsklopFragment.linijaHasPodsklopi(allPreventivniPregledi, linija)) {

            Log.d(TAG, "Linija " + linija.getLinija_SAP() + " has podsklopi - opening PodsklopFragment");
            // Ima podsklope → odpri PodsklopFragment
            PreventivniPreglediOpravilaPodsklopFragment fragment =
                    PreventivniPreglediOpravilaPodsklopFragment.newInstance(getContext(), linija);
            mainActivity.loadFragment(fragment);

        } else {
            Log.d(TAG, "Linija " + linija.getLinija_SAP() + " has NO podsklopi - opening OpravilaFragment directly");
            // Nima podsklopov → odpri direktno OpravilaFragment
            PreventivniPreglediOpravilaFragment fragment =
                    PreventivniPreglediOpravilaFragment.newInstance(getContext(), linija);
            mainActivity.loadFragment(fragment);
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
                filterLinije(newText);
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
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
                    allLinijeList = new ArrayList<>(response); // Copy for filtering
                    adapter.updateList(new ArrayList<Object>(linijeList));

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

    private void fetchPreventivniPregledi() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            apiManager.getPreventivniPregledi(new ApiManager.PreventivniPreglediCallback() {
                @Override
                public void onSuccess(List<PreventivniPregled> response) {
                    allPreventivniPregledi = response;

                    // Shrani v cache
                    String json = new Gson().toJson(response);
                    sharedPreferencesHelper.putString("AllPreventivniPreglediList", json);

                    Log.d(TAG, "Loaded " + response.size() + " preventivni pregledi for podsklop checking");
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Failed to load preventivni pregledi: " + errorMessage);
                    loadPreventivniPreglediFromPrefs(sharedPreferencesHelper);
                }
            });
        } else {
            loadPreventivniPreglediFromPrefs(sharedPreferencesHelper);
        }
    }

    private void loadPreventivniPreglediFromPrefs(SharedPreferencesHelper prefs) {
        String json = prefs.getString("AllPreventivniPreglediList", null);
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<PreventivniPregled>>() {}.getType();
                allPreventivniPregledi = new Gson().fromJson(json, type);
                Log.d(TAG, "Loaded " + (allPreventivniPregledi != null ? allPreventivniPregledi.size() : 0) +
                        " preventivni pregledi from cache");
            } catch (Exception e) {
                Log.e(TAG, "Error parsing cached preventivni pregledi: " + e.getMessage());
                allPreventivniPregledi = new ArrayList<>();
            }
        } else {
            Log.w(TAG, "No cached preventivni pregledi found");
            allPreventivniPregledi = new ArrayList<>();
        }
    }

    private void loadLinijeFromPrefs(SharedPreferencesHelper prefs) {
        String json = prefs.getString("LinijeList", null);
        if (json != null) {
            try {
                Type type = new TypeToken<List<Linija>>() {}.getType();
                linijeList = new Gson().fromJson(json, type);
                if (linijeList != null && !linijeList.isEmpty()) {
                    allLinijeList = new ArrayList<>(linijeList); // Copy for filtering
                    adapter.updateList(new ArrayList<Object>(linijeList));
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
        allLinijeList = new ArrayList<>();
        adapter.updateList(new ArrayList<Object>(linijeList));
        Toast.makeText(getContext(), "Ni podatkov za prikaz", Toast.LENGTH_SHORT).show();
    }

    private void filterLinije(String query) {
        if (allLinijeList == null) {
            Log.w(TAG, "allLinijeList is null, cannot filter");
            return;
        }

        List<Linija> filtered;

        if (query == null || query.trim().isEmpty()) {
            filtered = new ArrayList<>(allLinijeList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            filtered = allLinijeList.stream()
                    .filter(l ->
                            (l.getLinija_SAP() != null && l.getLinija_SAP().toLowerCase().contains(lowerQuery)) ||
                                    (l.getNaziv_linije() != null && l.getNaziv_linije().toLowerCase().contains(lowerQuery)) ||
                                    (l.getLokacija_naziv() != null && l.getLokacija_naziv().toLowerCase().contains(lowerQuery)) ||
                                    (l.getProstor_naziv() != null && l.getProstor_naziv().toLowerCase().contains(lowerQuery))
                    )
                    .collect(Collectors.toList());
        }

        linijeList = filtered;
        adapter.updateList(new ArrayList<Object>(filtered));
        Log.d(TAG, "Filtered " + filtered.size() + " items from " + allLinijeList.size());
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