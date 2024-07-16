package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Spinner;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentRegisterBinding;


public class RegisterFragment extends BaseFragment {

    public String TAG = "Register";
    FragmentRegisterBinding binding;
    Context context;
    private TextView textView;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance(Context context) {
        RegisterFragment fragment = new RegisterFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Get reference to the TextView
        textView = binding.addARole;

        // Set up the spinner with items
        Spinner spinner = binding.roleSpinner;
        String[] items = {"Nova rola", "Admin", "Vzdrževanje"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set an item selected listener for the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                // Perform action based on selected item
                if (selectedItem.equals("Nova rola")) {
                    // Make the textView visible
                    textView.setVisibility(View.VISIBLE);
                } else {
                    // Hide the textView for other items
                    textView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no item selected scenario if needed
            }
        });

        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }

}