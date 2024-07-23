package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.DataPickerDialog;
import com.example.machinenote.models.ListViewItem;
import com.example.machinenote.Utility.ListViewAdapter;
import com.example.machinenote.Utility.TextWatcherUtil;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentZastojiBinding;

import java.util.ArrayList;
import java.util.List;

public class ZastojiFragment extends BaseFragment {

    public String TAG = "Zastoji";
    private String[] linijaStringCache = {"Linija 1", "Linija 2", "Linija 3", "Linija 4"};
    private String[] sifrantStringCache = {"Šifrant 1", "Šifrant 2", "Šifrant 3", "Šifrant 4"};
    private String[] dataNames = {"Šifrant", "Ime delavca", "Razlog za zaustavitev stroja", "Opomba", "Linija"};
    private FragmentZastojiBinding binding;
    private Context context;
    private String idOfLine, sifrant;
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
        for(int i = 0; i < 5; i++){
            data.add(new ListViewItem(dataNames[i], false, i+1));
        }

        // Initialize the adapter
        adapter = new ListViewAdapter(requireContext(), R.layout.custom_listview_item, data);
        binding.listViewStoppages.setAdapter(adapter);

        // Set up TextWatchers
        TextWatcherUtil.addTextWatcherToEditText(context, binding.imeDelavca, data, 2, adapter); //textWatcher
        TextWatcherUtil.addTextWatcherToEditText(context, binding.razlogZaustavitveStroja, data, 3, adapter);
        TextWatcherUtil.addTextWatcherToEditText(context, binding.opomba, data, 4, adapter);

        binding.idOfLineBtn.setOnClickListener(view -> {

            int tmp = DataPickerDialog.showDialog(view, "Izberi Linijo!", linijaStringCache, requireContext(), binding.idOfLineBtn, adapter, 5);
            if (tmp != -1) {
                idOfLine = linijaStringCache[tmp];
            }
            //Log.d(TAG, String.valueOf(whichStringCache));
        });

        binding.sifrantBtn.setOnClickListener(view -> {
            int tmp = DataPickerDialog.showDialog(view, "Izberi Šifrant", sifrantStringCache, requireContext(), binding.sifrantBtn, adapter, 1);
            if(tmp != -1){
                sifrant = sifrantStringCache[tmp];
            }
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
