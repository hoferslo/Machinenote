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
import com.example.machinenote.customFragments.NarocilaBottomSheetFragment;
import com.example.machinenote.databinding.FragmentNarocilaBinding;
import com.example.machinenote.models.Narocila;

import java.util.ArrayList;
import java.util.List;

public class NarocilaFragment extends BaseFragment {

    FragmentNarocilaBinding binding;
    Context context;
    private ApiManager apiManager;
    private List<Narocila> narocilaList;
    private GenericAdapter<Narocila> adapter;

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

    private void fetchNarocila() {
        apiManager.getNarocila(new ApiManager.NarocilaCallback() {
            @Override
            public void onSuccess(List<Narocila> response) {
                narocilaList = response;
                adapter.updateList(narocilaList);
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

        // Refresh data when returning to this fragment
        fetchNarocila();
    }
}