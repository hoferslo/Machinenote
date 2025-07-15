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
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.RezervniDeliBottomSheetFragment;
import com.example.machinenote.databinding.FragmentRezervniDeliBinding;
import com.example.machinenote.models.RezervniDel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RezervniDeliFragment extends BaseFragment {

    private FragmentRezervniDeliBinding binding;
    private ApiManager apiManager;
    private List<RezervniDel> rezervniDelList;
    private GenericAdapter<RezervniDel> adapter;

    public RezervniDeliFragment() {}

    public static RezervniDeliFragment newInstance(Context context) {
        RezervniDeliFragment fragment = new RezervniDeliFragment();
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

        adapter = new GenericAdapter<>(getContext(), new ArrayList<>(), new GenericAdapter.OnItemClickListener<RezervniDel>() {
            @Override
            public void onItemClick(RezervniDel del) {
                // Create and show the bottom sheet
                RezervniDeliBottomSheetFragment bottomSheet =
                        RezervniDeliBottomSheetFragment.newInstance(getContext(), del);
                bottomSheet.show(getChildFragmentManager(), "RezervniDeliBottomSheet");
            }

            @Override
            public void onButtonClick(RezervniDel del) {

            }
        });

        recyclerView.setAdapter(adapter);

        binding.idOfDuty.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRezervniDeli(newText);
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
            }
        });

        fetchRezervniDeli();

        setupTabNavigation();

        return binding.getRoot();
    }

    private void setupTabNavigation() {
        binding.switchTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                if (checkedId == R.id.tabSmallMaterialsBtn) {
                    FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

                    // Pop current fragment and replace
                    fragmentManager.popBackStack();

                    Fragment drobniMaterialiFragment = com.example.machinenote.fragments.DrobniMaterialiFragment.newInstance(mainActivity);
                    mainActivity.loadFragment(drobniMaterialiFragment);
                }
            }
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
            } else {
                Log.e(TAG, "Parsed rezervniDelList is null.");
            }
        } else {
            Log.e(TAG, "No cached data found.");
        }
    }

    private void filterRezervniDeli(String query) {
        if (rezervniDelList != null) {
            List<RezervniDel> filtered = rezervniDelList.stream()
                    .filter(d -> query == null || query.isEmpty()
                            || d.getArtikel().toLowerCase().contains(query.toLowerCase())
                            || d.getArtikel_dolgi_text().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateList(filtered);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).binding.toolbarTitle.setText(TAG);
    }
}
