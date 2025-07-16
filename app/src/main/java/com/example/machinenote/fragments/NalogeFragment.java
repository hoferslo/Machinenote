package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.GenericAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentNalogeBinding;
import com.example.machinenote.models.Naloga;

import java.util.ArrayList;
import java.util.List;

public class NalogeFragment extends BaseFragment {

    FragmentNalogeBinding binding;
    Context context;
    private ApiManager apiManager;
    private List<Naloga> nalogaList;
    private GenericAdapter<Naloga> adapter;

    public NalogeFragment() {
        // Required empty public constructor
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

        adapter = new GenericAdapter<>(getContext(), new ArrayList<>(), new GenericAdapter.OnItemClickListener<Naloga>() {
            @Override
            public void onItemClick(Naloga item) {
                // Handle item click
            }

            @Override
            public void onButtonClick(Naloga item) {
                // Handle button click if needed
            }
        });

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

        // Cancel button (if exists in your layout)
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
                adapter.updateList(nalogaList);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Error: " + errorMessage);
                // Optionally load saved data from SharedPreferences here
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}