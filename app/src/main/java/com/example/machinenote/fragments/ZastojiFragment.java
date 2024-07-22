package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.ListViewItem;
import com.example.machinenote.Utility.ListViewAdapter;
import com.example.machinenote.Utility.TextWatcherUtil;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentZastojiBinding;

import java.util.ArrayList;
import java.util.List;

public class ZastojiFragment extends BaseFragment {

    public String TAG = "Zastoji";
    private FragmentZastojiBinding binding;
    private Context context;
    private List<ListViewItem> data;
    private ListViewAdapter adapter;

    public ZastojiFragment() {
        // Required empty public constructor
    }

    public static ZastojiFragment newInstance(Context context) {
        ZastojiFragment fragment = new ZastojiFragment();
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
        binding = FragmentZastojiBinding.inflate(getLayoutInflater());

        // Initialize the data list
        data = new ArrayList<>();
        data.add(new ListViewItem("Ime delavca", false, 1));
        data.add(new ListViewItem("Razlog za zaustavitev stroja", false, 2));
        data.add(new ListViewItem("Opomba", false, 3));

        // Initialize the adapter
        adapter = new ListViewAdapter(requireContext(), R.layout.custom_listview_item, data);

        // Set the adapter to the ListView
        binding.listViewStoppages.setAdapter(adapter);

        // Set up TextWatchers
        TextWatcherUtil.addTextWatcherToEditText(context, binding.imeDelavca, data, 1, adapter);
        TextWatcherUtil.addTextWatcherToEditText(context, binding.razlogZaustavitveStroja, data, 2, adapter);
        TextWatcherUtil.addTextWatcherToEditText(context, binding.opomba, data, 3, adapter);

        binding.cancelButton.setOnClickListener(view -> {
            // Handle cancel button click
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }
}
