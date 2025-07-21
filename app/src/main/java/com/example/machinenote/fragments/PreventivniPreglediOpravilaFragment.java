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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentPreventivniPreglediOpravilaBinding;
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.models.PregledOpravilo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PreventivniPreglediOpravilaFragment extends BaseFragment {

    private FragmentPreventivniPreglediOpravilaBinding binding;
    private ApiManager apiManager;
    private List<PregledOpravilo> opravilaList;
    private GenericAdapter<PregledOpravilo> adapter;
    private PreventivniPregled parentPregled;

    public PreventivniPreglediOpravilaFragment() {}

    public static PreventivniPreglediOpravilaFragment newInstance(Context context, PreventivniPregled pregled) {
        PreventivniPreglediOpravilaFragment fragment = new PreventivniPreglediOpravilaFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.parentPregled = pregled;
        fragment.TAG = "Opravila - " + pregled.getNaziv();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPreventivniPreglediOpravilaBinding.inflate(inflater, container, false);

        setupUI();
        setupRecyclerView();
        setupSearch();
        fetchOpravila();

        return binding.getRoot();
    }

    private void setupUI() {
        // Prikaži podatke o preventivnem pregledu
        binding.textPregledNaziv.setText(parentPregled.getNaziv());
        binding.textPregledDatum.setText(parentPregled.getDatum());
        binding.textPregledLinija.setText(parentPregled.getLinijaSAP() + " - " + parentPregled.getNazivLinije());
        binding.textPregledStatus.setText(parentPregled.getStatus());

        // Nastavi barvo statusa
        setStatusColor(parentPregled.getStatus());

        // Progress bar
        updateProgress();
    }

    private void setStatusColor(String status) {
        int colorRes;
        switch (status.toLowerCase()) {
            case "aktiven":
                colorRes = R.color.action_destructive;
                break;
            case "v teku":
                colorRes = R.color.action_secondary;
                break;
            case "zakljucen":
                colorRes = R.color.action_success;
                break;
            default:
                colorRes = R.color.action_secondary;
        }
        binding.textPregledStatus.setTextColor(getResources().getColor(colorRes, null));
    }

    private void updateProgress() {
        if (opravilaList != null) {
            long completed = opravilaList.stream()
                    .filter(o -> "Zakljuceno".equals(o.getStatus()))
                    .count();

            binding.progressBar.setMax(opravilaList.size());
            binding.progressBar.setProgress((int) completed);
            binding.textProgress.setText(completed + "/" + opravilaList.size() + " opravil");
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.recyclerViewOpravila;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = GenericAdapter.create(
                getContext(),
                new ArrayList<>(),
                new GenericAdapter.OnItemClickListener<PregledOpravilo>() {
                    @Override
                    public void onItemClick(PregledOpravilo opravilo) {
                        // Odpri dialog za urejanje opravila
                        showOpraviloEditDialog(opravilo);
                    }

                    @Override
                    public void onButtonClick(PregledOpravilo opravilo) {
                        // Hitro označevanje kot končano
                        markOpraviloAsCompleted(opravilo);
                    }
                },
                "OpisOpravila",
                "Status",
                "Sklop_Linije",
                "Trajanje_STD_Min",
                "ActVredParameter"
        );

        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterOpravila(newText);
                return true;
            }
        });

        binding.searchView.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.searchView.clearFocus();
            }
        });
    }

    private void fetchOpravila() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        String cacheKey = "OpravilaPregled_" + parentPregled.getId();

        if (mainActivity.serverConnection) {
            apiManager.getOpravilaForPregled(parentPregled.getId(), new ApiManager.OpravilaCallback() {
                @Override
                public void onSuccess(List<PregledOpravilo> response) {
                    opravilaList = response;
                    adapter.updateList(opravilaList);
                    updateProgress();

                    // Cache the data
                    String json = new Gson().toJson(opravilaList);
                    sharedPreferencesHelper.putString(cacheKey, json);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "API failure: " + errorMessage);
                    loadOpravilaFromCache(sharedPreferencesHelper, cacheKey);
                }
            });
        } else {
            loadOpravilaFromCache(sharedPreferencesHelper, cacheKey);
        }
    }

    private void loadOpravilaFromCache(SharedPreferencesHelper prefs, String cacheKey) {
        String json = prefs.getString(cacheKey, null);
        if (json != null) {
            Type type = new TypeToken<List<PregledOpravilo>>() {}.getType();
            opravilaList = new Gson().fromJson(json, type);
            if (opravilaList != null) {
                adapter.updateList(opravilaList);
                updateProgress();
            } else {
                Log.e(TAG, "Parsed opravilaList is null.");
            }
        } else {
            Log.e(TAG, "No cached data found for key: " + cacheKey);
            Toast.makeText(getContext(), "Ni podatkov o opravilih", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterOpravila(String query) {
        if (opravilaList != null) {
            List<PregledOpravilo> filtered = opravilaList.stream()
                    .filter(o -> query == null || query.isEmpty()
                            || o.getOpisOpravila().toLowerCase().contains(query.toLowerCase())
                            || o.getSklopLinije().toLowerCase().contains(query.toLowerCase())
                            || o.getStatus().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateList(filtered);
        }
    }

    private void showOpraviloEditDialog(PregledOpravilo opravilo) {
        // TODO: Implementiraj dialog za urejanje opravila
        // Dialog naj omogoča:
        // - Spremembo statusa
        // - Vnos dejanske vrednosti parametra
        // - Vnos časa izvajanja
        // - Vnos opomb
        Toast.makeText(getContext(), "Edit dialog za: " + opravilo.getOpisOpravila(), Toast.LENGTH_SHORT).show();
    }

    private void markOpraviloAsCompleted(PregledOpravilo opravilo) {
        // Hitro označevanje kot končano
        opravilo.setStatus("Zakljuceno");

        // Pošlji na API
        apiManager.updateOpraviloStatus(opravilo.getId(), opravilo, new ApiManager.UpdateCallback() {
            @Override
            public void onSuccess(String message) {
                adapter.notifyDataSetChanged();
                updateProgress();
                Toast.makeText(getContext(), "Opravilo označeno kot končano", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(List<PreventivniPregled> response) {

            }

            @Override
            public void onFailure(String errorMessage) {
                // Povrni status nazaj
                opravilo.setStatus("Ni zaceto"); // ali prejšnji status
                Log.e(TAG, "Failed to update opravilo: " + errorMessage);
                Toast.makeText(getContext(), "Napaka pri posodabljanju", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).binding.toolbarTitle.setText(TAG);
    }
}