package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDodajNalogoBinding;

public class DodajNalogoFragment extends BaseFragment {

    private FragmentDodajNalogoBinding binding;
    private Context context;

    public DodajNalogoFragment() {
        // Required empty public constructor
    }

    public static DodajNalogoFragment newInstance(Context context) {
        DodajNalogoFragment fragment = new DodajNalogoFragment();
        fragment.context = context;
        fragment.TAG = context.getString(R.string.dodaj_nalogo); // ali "Dodaj nalogo"
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDodajNalogoBinding.inflate(getLayoutInflater());

        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        // Tab navigation
        binding.tabNalogeBtn.setOnClickListener(v -> {
            // Switch back to NalogeFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(NalogeFragment.newInstance(context));
            }
        });

        binding.tabVnosNalogeBtn.setOnClickListener(v -> {
            // Already on this fragment, do nothing or refresh
        });

        // Action buttons
        binding.prekliciBtn.setOnClickListener(v -> {
            // Cancel - go back to NalogeFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(NalogeFragment.newInstance(context));
            }
        });

        binding.shraniBtn.setOnClickListener(v -> {
            // Save naloga
            saveNaloga();
        });

        binding.rokEditText.setOnClickListener(v -> {
            // Open date picker
            showDatePicker();
        });

        binding.dodajSlikoBtn.setOnClickListener(v -> {
            // Add image functionality
            addImage();
        });

        binding.odstranislikoBtn.setOnClickListener(v -> {
            // Remove image
            removeImage();
        });
    }

    private void saveNaloga() {
        String vzdrzevalec = binding.vzdrzevalecEditText.getText().toString().trim();
        String naslov = binding.naslovEditText.getText().toString().trim();
        String rok = binding.rokEditText.getText().toString().trim();
        String opis = binding.opisEditText.getText().toString().trim();

        if (vzdrzevalec.isEmpty() || naslov.isEmpty() || rok.isEmpty()) {
            Toast.makeText(context, "Prosimo, izpolnite vsa obvezna polja", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement actual saving logic with ApiManager
        // apiManager.createNaloga(vzdrzevalec, naslov, rok, opis, callback);

        Toast.makeText(context, "Naloga shranjena", Toast.LENGTH_SHORT).show();

        // Go back to NalogeFragment
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.loadFragment(NalogeFragment.newInstance(context));
        }
    }

    private void showDatePicker() {
        // TODO: Implement date picker dialog
        Toast.makeText(context, "Date picker - TODO", Toast.LENGTH_SHORT).show();
    }

    private void addImage() {
        // TODO: Implement image picker (camera/gallery)
        Toast.makeText(context, "Add image - TODO", Toast.LENGTH_SHORT).show();
    }

    private void removeImage() {
        binding.imagePreview.setVisibility(View.GONE);
        binding.odstranislikoBtn.setVisibility(View.GONE);
        binding.dodajSlikoBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}