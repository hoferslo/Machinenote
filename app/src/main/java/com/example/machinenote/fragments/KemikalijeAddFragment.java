package com.example.machinenote.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentKemikalijeAddBinding;
import com.example.machinenote.models.Kemikalija;
import com.example.machinenote.models.OmaraKemikalije;
import com.example.machinenote.models.PolicaKemikalije;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KemikalijeAddFragment extends BaseFragment {

    FragmentKemikalijeAddBinding binding;
    Context context;
    ApiManager apiManager;
    private Date selectedDate;
    private Calendar calendar;

    private List<PolicaKemikalije> policaKemikalijeList = new ArrayList<>();
    private List<OmaraKemikalije> omaraKemikalijeList = new ArrayList<>();
    private List<PolicaKemikalije> filteredPolicaList = new ArrayList<>();
    private ArrayAdapter<String> policaAdapter;
    private ArrayAdapter<String> omaraAdapter;

    public KemikalijeAddFragment() {
        // Required empty public constructor
    }

    public static KemikalijeAddFragment newInstance(Context context) {
        KemikalijeAddFragment fragment = new KemikalijeAddFragment();
        fragment.context = context;
        fragment.TAG = "Dodaj Kemikalijo";
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentKemikalijeAddBinding.inflate(getLayoutInflater());
        apiManager = new ApiManager(context);

        setupSpinners();
        setupDatePicker();
        setupButtons();

        // Load data from database
        loadOmaraData();
        loadPolicaData();

        return binding.getRoot();
    }

    private void setupSpinners() {
        // Setup Enota (Unit) Spinner - static data
        String[] units = {"g", "kg", "mg", "L", "mL", "mol"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                units
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.enotaSpinner.setAdapter(unitAdapter);

        // Setup Omara (Cabinet) Spinner - will be populated from database
        omaraAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        omaraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.omaraSpinner.setAdapter(omaraAdapter);

        // Setup Polica (Shelf) Spinner - will be populated based on selected Omara
        policaAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        policaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.policaSpinner.setAdapter(policaAdapter);

        // When Omara is selected, filter Police
        binding.omaraSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterPolicaByOmara(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadPolicaData() {
        apiManager.fetchAllPoliceKemikalije(new ApiManager.PoliceListCallback() {
            @Override
            public void onSuccess(List<PolicaKemikalije> police) {
                policaKemikalijeList = police;
                // Don't update spinner yet - wait for omara selection
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Napaka pri nalaganju polic: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOmaraData() {
        apiManager.fetchAllOmareKemikalije(new ApiManager.OmareListCallback() {
            @Override
            public void onSuccess(List<OmaraKemikalije> omare) {
                omaraKemikalijeList = omare;
                updateOmaraSpinner();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Napaka pri nalaganju omar: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOmaraSpinner() {
        List<String> omaraNames = new ArrayList<>();
        for (OmaraKemikalije omaraKemikalije : omaraKemikalijeList) {
            omaraNames.add(omaraKemikalije.getOmara_ime());
        }

        omaraAdapter.clear();
        omaraAdapter.addAll(omaraNames);
        omaraAdapter.notifyDataSetChanged();
    }

    private void updatePolicaSpinner() {
        List<String> policaNames = new ArrayList<>();
        for (PolicaKemikalije policaKemikalije : filteredPolicaList) {
            policaNames.add(policaKemikalije.getPolica());
        }

        policaAdapter.clear();
        policaAdapter.addAll(policaNames);
        policaAdapter.notifyDataSetChanged();
    }

    private void filterPolicaByOmara(int omaraPosition) {
        filteredPolicaList.clear();

        if (omaraPosition >= 0 && omaraPosition < omaraKemikalijeList.size()) {
            int selectedOmaraId = omaraKemikalijeList.get(omaraPosition).getId();

            // Filter police that belong to selected omara
            for (PolicaKemikalije polica : policaKemikalijeList) {
                if (polica.getOmara_ID() != null && polica.getOmara_ID() == selectedOmaraId) {
                    filteredPolicaList.add(polica);
                }
            }
        }

        updatePolicaSpinner();
    }

    private void setupDatePicker() {
        binding.rokIzteka.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate = calendar.getTime();
                        updateDateButton();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateDateButton() {
        if (selectedDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            binding.rokIzteka.setText(dateFormat.format(selectedDate));
        }
    }

    private void setupButtons() {
        // Cancel button
        binding.prekliciBtn.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        // Save button
        binding.shraniBtn.setOnClickListener(v -> saveKemikalija());
    }

    private void saveKemikalija() {
        // Validate required fields
        String imeSLO = binding.imeSLO.getText().toString().trim();
        String imeENG = binding.imeENG.getText().toString().trim();
        String amountStr = binding.amountOfArticle.getText().toString().trim();
        String formula = binding.formula.getText().toString().trim();
        String cas = binding.CAS.getText().toString().trim();

        if (TextUtils.isEmpty(imeSLO)) {
            Toast.makeText(context, "Vnesite slovensko ime kemikalije", Toast.LENGTH_SHORT).show();
            binding.imeSLO.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(context, "Vnesite količino", Toast.LENGTH_SHORT).show();
            binding.amountOfArticle.requestFocus();
            return;
        }

        // Create Kemikalija object
        Kemikalija kemikalija = new Kemikalija();
        kemikalija.setIme_SLO(imeSLO);
        kemikalija.setIme_ENG(imeENG);

        // Set amount with unit
        String unit = binding.enotaSpinner.getSelectedItem().toString();
        kemikalija.setTeza(amountStr + " " + unit);

        kemikalija.setFormula(formula);
        kemikalija.setCas_stevilo(cas);

        // Get selected Polica ID (from filtered list)
        int policaPosition = binding.policaSpinner.getSelectedItemPosition();
        if (policaPosition >= 0 && policaPosition < filteredPolicaList.size()) {
            PolicaKemikalije selectedPolica = filteredPolicaList.get(policaPosition);
            kemikalija.setPolica_ID(selectedPolica.getId());
        }

        kemikalija.setRok_uporabe(selectedDate);

        String firma = binding.firma.getText().toString().trim();
        kemikalija.setFirma(firma);

        String agregatnoStanje = binding.agregatnoStanje.getText().toString().trim();
        kemikalija.setAgregatno_stanje(agregatnoStanje);

        String identUnichem = binding.identUnichem.getText().toString().trim();
        if (!TextUtils.isEmpty(identUnichem)) {
            try {
                kemikalija.setIdent_Unichem(Integer.parseInt(identUnichem));
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Unichem identifikator mora biti številka", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String opombe = binding.opombe.getText().toString().trim();
        kemikalija.setOpombe(opombe);

        // Save to API
        apiManager.createKemikalija(kemikalija, new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Kemikalija uspešno shranjena", Toast.LENGTH_SHORT).show();
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.onBackPressed();
                } else {
                    Toast.makeText(context, "Napaka pri shranjevanju: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(context, "Napaka: " + t.getMessage(), Toast.LENGTH_LONG).show();
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