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
import com.example.machinenote.Utility.ImenikAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.ImenikBottomSheetFragment;
import com.example.machinenote.databinding.FragmentImenikBinding;
import com.example.machinenote.models.Imenik;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImenikFragment extends BaseFragment implements ImenikAdapter.OnItemClickListener {

    private FragmentImenikBinding binding;
    private ApiManager apiManager;
    private List<Imenik> imenikList;
    private ImenikAdapter adapter;

    public ImenikFragment() {
        // Required empty public constructor
    }

    public static ImenikFragment newInstance(Context context) {
        ImenikFragment fragment = new ImenikFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.TAG = context.getString(R.string.tag_knjizenje);
        return fragment;
    }

    @Override
    public void onItemClick(Imenik imenik) {
        ImenikBottomSheetFragment bottomSheetFragment = ImenikBottomSheetFragment.newInstance(requireActivity(), imenik);
        bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());
    }

    @Override
    public void onButtonClick(Imenik imenik) {
        // Handle button click action, for example:
        Toast.makeText(getContext(), "Action button clicked for " + imenik.getNazivPodjetja(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentImenikBinding.inflate(inflater, container, false);

        // Set up RecyclerView
        RecyclerView recyclerView = binding.scrollLv;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ImenikAdapter(getContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        binding.idOfDuty.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterImenik(newText);
                return true;
            }
        });

        binding.idOfDuty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                binding.idOfDuty.clearFocus();
            }
        });

        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });
        //todo eksperimentiraj, kako deluje, če je prepočasno, ko ni interneta, preveri, če je povezava tukaj
        fetchImenik();

        return binding.getRoot();
    }

    private void fetchImenik() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());

        if (mainActivity.serverConnection) {
            // If there's a server connection, make the API call
            apiManager.getImenik(new ApiManager.ImenikCallback() {
                @Override
                public void onSuccess(List<Imenik> response) {
                    imenikList = response;
                    adapter.updateList(imenikList);

                    // Convert the List to a JSON string using Gson (SharedPreferencesHelper already uses Gson)
                    Gson gson = new Gson();
                    String imenikJson = gson.toJson(imenikList);

                    // Save the JSON string to SharedPreferences
                    sharedPreferencesHelper.putString("ImenikList", imenikJson);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Error: " + errorMessage);
                    // Load the saved data from SharedPreferences in case of failure
                    loadImenikFromSharedPrefs(sharedPreferencesHelper);
                }
            });
        } else {
            // No connection, load from SharedPreferences
            loadImenikFromSharedPrefs(sharedPreferencesHelper);
        }
    }

    // Load the data from SharedPreferences
    private void loadImenikFromSharedPrefs(SharedPreferencesHelper sharedPreferencesHelper) {
        String imenikJson = sharedPreferencesHelper.getString("ImenikList", null);

        if (imenikJson != null) {
            // Convert the JSON string back to a List<Imenik> using Gson
            Gson gson = new Gson();
            Type type = new TypeToken<List<Imenik>>() {
            }.getType();
            List<Imenik> imenikList = gson.fromJson(imenikJson, type);

            if (imenikList != null) {
                adapter.updateList(imenikList);
            } else {
                Log.e(TAG, "No data found in SharedPreferences.");
            }
        } else {
            Log.e(TAG, "No saved data found in SharedPreferences.");
        }
    }

    private void filterImenik(String query) {
        if (imenikList != null) {
            List<Imenik> filteredList = imenikList.stream()
                    .filter(imenik -> (query == null || query.isEmpty()) ||
                            imenik.getNazivPodjetja().toLowerCase().contains(query.toLowerCase()) ||
                            imenik.getKontaktnaOseba().toLowerCase().contains(query.toLowerCase()) ||
                            imenik.getMail().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateList(filteredList);
        }
    }
}
