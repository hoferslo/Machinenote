package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.DataPickerDialog;
import com.example.machinenote.Utility.ListViewAdapter;
import com.example.machinenote.Utility.TextWatcherUtil;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentZastojiBinding;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.ListViewItem;
import com.example.machinenote.models.Zastoj;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZastojiFragment extends BaseFragment {

    public String TAG = "Zastoji";
    private List<Linija> linije;
    private String[] sifrantStringCache = {"Šifrant 1", "Šifrant 2", "Šifrant 3", "Šifrant 4"};
    private Zastoj zastoj;
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

        String[] dataNames = {"Šifrant", "Ime delavca", "Razlog za zaustavitev stroja", "Opomba", "Linija"};

        data = new ArrayList<>();
        for(int i = 0; i < dataNames.length; i++){
            data.add(new ListViewItem(dataNames[i], false, i+1));
        }

        // Initialize the adapter
        adapter = new ListViewAdapter(requireContext(), R.layout.custom_listview_item, data);
        binding.listViewStoppages.setAdapter(adapter);

        // Set up TextWatchers
        TextWatcherUtil.addTextWatcherToEditText(binding.imeDelavca,2, adapter); //textWatcher
        TextWatcherUtil.addTextWatcherToEditText(binding.razlogZaustavitveStroja, 3, adapter);
        TextWatcherUtil.addTextWatcherToEditText(binding.opomba, 4, adapter);

        binding.idOfLineBtn.setOnClickListener(view -> {

            int tmp = DataPickerDialog.showDialog(view, "Izberi Linijo!",
                    linije.stream().map(Linija::getLinija_SAP).toArray(String[]::new),
                    requireContext(), binding.idOfLineBtn, adapter, 5);
            if (tmp != -1) {
                idOfLine = linije.get(tmp).getLinija_SAP();
            }
            //Log.d(TAG, String.valueOf(whichStringCache));
        });

        binding.sifrantBtn.setOnClickListener(view -> {
            int tmp = DataPickerDialog.showDialog(view, "Izberi Šifrant", sifrantStringCache, requireContext(), binding.sifrantBtn, adapter, 1);
            if(tmp != -1){
                sifrant = sifrantStringCache[tmp];
            }
        });

        binding.stoppagesLl.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Remove the listener to avoid multiple calls
                        binding.stoppagesLl.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int height = binding.stoppagesLl.getHeight();

                        if (height < binding.stoppagesLl.getMinimumHeight()) {
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (int) (80 * getResources().getDisplayMetrics().density) // Convert 64dp to pixels
                            );
                            int marginBottom = (int) (16 * getResources().getDisplayMetrics().density); // Convert 16dp to pixels
                            layoutParams.setMargins(0, 0, 0, marginBottom);
                            binding.stoppagesLl.setLayoutParams(layoutParams);
                        }
                    }
                }
        );

        ApiManager apiManager = new ApiManager(context);

        {
            // Fetch zastoji data
            apiManager.getZastoji(new ApiManager.ZastojiCallback() {
                @Override
                public void onSuccess(List<Zastoj> zastoji) {
                    Log.d("mhm", "zastoji: " + zastoji);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(context, "Failed to fetch zastoji: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        {
            apiManager.fetchLinije(new ApiManager.LinijeCallback() {
                @Override
                public void onSuccess(List<Linija> response) {

                    linije = response;
                }

                @Override
                public void onFailure(String errorMessage) {
                    System.err.println("Error: " + errorMessage);
                }
            });
        }

        Zastoj zastoj = new Zastoj(
                "Test Maintenance",             // vzdrzevalec
                "Test Line",                    // linija
                "Test Code",                    // sifrant
                "Test Note",                    // opomba
                "Test Reason",                  // razlog
                "2024-07-01 08:00:00",         // zacetek
                "2024-07-01 10:00:00",         // konec
                "Test Worker",                  // delavec
                "Test SAP Line",                // SAP_linije
                "Test Image",                   // slike
                "YES",                          // dezurstvo
                120,                            // trajanje_zastoja_v_minutah
                2024,                           // leto
                7,                              // mesec
                "QR123"                         // QR_koda_vnos
        );

        binding.sendBtn.setOnClickListener(v -> apiManager.sendZastoj(zastoj, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                } else {
                    // Handle failure
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle error
            }
        }));

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);

    }
}
