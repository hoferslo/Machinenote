package com.example.machinenote.customFragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.machinenote.ApiManager;
import com.example.machinenote.R;
import com.example.machinenote.databinding.FragmentKemikalijeBottomSheetBinding;
import com.example.machinenote.models.Kemikalija;
import com.example.machinenote.models.OmaraKemikalije;
import com.example.machinenote.models.PolicaKemikalije;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KemikalijeBottomSheetFragment extends BottomSheetDialogFragment {

    public FragmentKemikalijeBottomSheetBinding binding;
    private Context context;
    private Kemikalija kemikalija;
    private SimpleDateFormat dateFormat;
    private boolean isEditMode = false;
    private OnKemikalijaUpdateListener updateListener;
    ApiManager apiManager;
    private List<PolicaKemikalije> policaKemikalijeList = new ArrayList<>();
    private List<OmaraKemikalije> omaraKemikalijeList = new ArrayList<>();
    private List<PolicaKemikalije> filteredPolicaList = new ArrayList<>();
    private ArrayAdapter<String> policaAdapter;
    private ArrayAdapter<String> omaraAdapter;

    // Interface za callback ko se shrani
    public interface OnKemikalijaUpdateListener {
        void onKemikalijaUpdated(Kemikalija updatedKemikalija);
    }

    public static KemikalijeBottomSheetFragment newInstance(Context context, Kemikalija kemikalija, OnKemikalijaUpdateListener listener) {
        KemikalijeBottomSheetFragment fragment = new KemikalijeBottomSheetFragment();
        fragment.kemikalija = kemikalija;
        fragment.context = context;
        fragment.updateListener = listener;
        return fragment;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentKemikalijeBottomSheetBinding.inflate(inflater, container, false);
        dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        apiManager = new ApiManager(context);

        // Nastavi spinnerje
        setupSpinners();

        // Prikaži podatke
        displayData();

        // Setup gumbov
        setupButtons();
        loadOmaraData();
        loadPolicaData();

        return binding.getRoot();
    }

    private void setupSpinners() {
        String[] units = {"g", "kg", "mg", "L", "mL", "mol"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                context,
                R.layout.item_spinner_layout,
                units
        );
        unitAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
        binding.enotaSpinner.setAdapter(unitAdapter);

        // Setup Omara (Cabinet) Spinner - will be populated from database
        omaraAdapter = new ArrayAdapter<>(
                context,
                R.layout.item_spinner_layout,
                new ArrayList<>()
        );
        omaraAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
        binding.omaraSpinner.setAdapter(omaraAdapter);

        // Setup Polica (Shelf) Spinner - will be populated based on selected Omara
        policaAdapter = new ArrayAdapter<>(
                context,
                R.layout.item_spinner_layout,
                new ArrayList<>()
        );
        policaAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
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

    private void displayData() {
        // Prikaži v view mode
        binding.chemicalIdHeader.setText("Podrobnosti o kemikaliji");

        setFieldValue(binding.tvImeSlo, "Slovensko ime: ", kemikalija.getIme_SLO());
        setFieldValue(binding.tvImeEng, "Angleško ime: ", kemikalija.getIme_ENG());
        setFieldValue(binding.tvFormula, "Formula: ", kemikalija.getFormula());
        setFieldValue(binding.tvCas, "CAS številka: ", kemikalija.getCas_stevilo());
        setFieldValue(binding.tvFirma, "Firma: ", kemikalija.getFirma());
        setFieldValue(binding.tvAgregatno, "Agregatno stanje: ", kemikalija.getAgregatno_stanje());
        setFieldValue(binding.tvPolica, "Polica: ", kemikalija.getPolica());
        setFieldValue(binding.tvOmara, "Omara: ", kemikalija.getOmara());
        setFieldValue(binding.tvProgram, "Program: ", kemikalija.getProgram());
        setFieldValue(binding.tvTeza, "Teža: ", kemikalija.getTeza());

        if (kemikalija.getRok_uporabe() != null) {
            binding.tvRokUporabe.setText("Rok uporabe: " + dateFormat.format(kemikalija.getRok_uporabe()));
        } else {
            binding.tvRokUporabe.setText("Rok uporabe: Ni podatka");
        }

        if (kemikalija.getIdent_Unichem() > 0) {
            binding.tvIdentUnichem.setText("Ident Unichem: " + kemikalija.getIdent_Unichem());
        } else {
            binding.tvIdentUnichem.setText("Ident Unichem: Ni podatka");
        }

        if (kemikalija.getOpombe() != null && !kemikalija.getOpombe().isEmpty()) {
            binding.tvOpombe.setText(kemikalija.getOpombe());
        } else {
            binding.tvOpombe.setText("Ni podatka");
        }

        // Nastavi edit polja
        populateEditFields();
    }

    private void setFieldValue(android.widget.TextView textView, String label, String value) {
        if (value != null && !value.isEmpty()) {
            textView.setText(label + value);
        } else {
            textView.setText(label + "Ni podatka");
        }
    }

    private void populateEditFields() {
        binding.etImeSlo.setText(kemikalija.getIme_SLO());
        binding.etImeEng.setText(kemikalija.getIme_ENG());
        binding.etFormula.setText(kemikalija.getFormula());
        binding.etCas.setText(kemikalija.getCas_stevilo());
        binding.etFirma.setText(kemikalija.getFirma());
        binding.etAgregatno.setText(kemikalija.getAgregatno_stanje());
        binding.etIdentUnichem.setText(kemikalija.getIdent_Unichem() > 0 ? String.valueOf(kemikalija.getIdent_Unichem()) : "");
        binding.etOpombe.setText(kemikalija.getOpombe());

        // Nastavi spinner vrednosti
        if (kemikalija.getOmara() != null) {
            int omaraPos = ((ArrayAdapter<String>)binding.omaraSpinner.getAdapter()).getPosition(kemikalija.getOmara());
            if (omaraPos >= 0) binding.omaraSpinner.setSelection(omaraPos);
        }

        if (kemikalija.getPolica() != null) {
            int policaPos = ((ArrayAdapter<String>)binding.policaSpinner.getAdapter()).getPosition(kemikalija.getPolica());
            if (policaPos >= 0) binding.policaSpinner.setSelection(policaPos);
        }

        // Parsaj težo in enoto
        if (kemikalija.getTeza() != null && !kemikalija.getTeza().isEmpty()) {
            String[] parts = kemikalija.getTeza().split(" ");
            if (parts.length > 0) {
                binding.etKolicina.setText(parts[0]);
                if (parts.length > 1) {
                    int enotaPos = ((ArrayAdapter<String>)binding.enotaSpinner.getAdapter()).getPosition(parts[1]);
                    if (enotaPos >= 0) binding.enotaSpinner.setSelection(enotaPos);
                }
            }
        }
    }

    private void setupButtons() {
        // Preklopi na edit mode
        binding.btnUredi.setOnClickListener(v -> toggleEditMode(true));

        // Shrani spremembe
        binding.btnShrani.setOnClickListener(v -> saveChanges());

        // Preklici urejanje
        binding.btnPreklici.setOnClickListener(v -> toggleEditMode(false));

        // Datum picker
        binding.btnRokUporabe.setOnClickListener(v -> showDatePicker());

        // Začetno stanje - view mode
        toggleEditMode(false);
    }

    private void toggleEditMode(boolean editMode) {
        isEditMode = editMode;

        if (editMode) {
            binding.viewModeContainer.setVisibility(View.GONE);
            binding.btnUredi.setVisibility(View.GONE);

            binding.editModeContainer.setVisibility(View.VISIBLE);
            binding.editButtonsContainer.setVisibility(View.VISIBLE);

            binding.chemicalIdHeader.setText("Uredi kemikalijo");
        } else {

            binding.viewModeContainer.setVisibility(View.VISIBLE);
            binding.btnUredi.setVisibility(View.VISIBLE);

            binding.editModeContainer.setVisibility(View.GONE);
            binding.editButtonsContainer.setVisibility(View.GONE);

            binding.chemicalIdHeader.setText("Podrobnosti o kemikaliji");

            populateEditFields();
        }
    }

    private void saveChanges() {
        if (binding.etImeSlo.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Slovensko ime je obvezno!", Toast.LENGTH_SHORT).show();
            return;
        }

        kemikalija.setIme_SLO(binding.etImeSlo.getText().toString().trim());
        kemikalija.setIme_ENG(binding.etImeEng.getText().toString().trim());
        kemikalija.setFormula(binding.etFormula.getText().toString().trim());
        kemikalija.setCas_stevilo(binding.etCas.getText().toString().trim());
        kemikalija.setFirma(binding.etFirma.getText().toString().trim());
        kemikalija.setAgregatno_stanje(binding.etAgregatno.getText().toString().trim());

        int omaraPosition = binding.omaraSpinner.getSelectedItemPosition();
        if (omaraPosition >= 0 && omaraPosition < omaraKemikalijeList.size()) {
            kemikalija.setOmara(omaraKemikalijeList.get(omaraPosition).getOmara_ime());
        }

        int policaPosition = binding.policaSpinner.getSelectedItemPosition();
        if (policaPosition >= 0 && policaPosition < filteredPolicaList.size()) {
            PolicaKemikalije selectedPolica = filteredPolicaList.get(policaPosition);
            kemikalija.setPolica_ID(selectedPolica.getId());
            kemikalija.setPolica(selectedPolica.getPolica());
        }

        // Teža
        String kolicina = binding.etKolicina.getText().toString().trim();
        String enota = binding.enotaSpinner.getSelectedItem().toString();
        kemikalija.setTeza(kolicina + " " + enota);

        // Ident
        try {
            String identStr = binding.etIdentUnichem.getText().toString().trim();
            if (!identStr.isEmpty()) {
                kemikalija.setIdent_Unichem(Integer.parseInt(identStr));
            }
        } catch (NumberFormatException e) {
            kemikalija.setIdent_Unichem(0);
        }

        // Opombe
        kemikalija.setOpombe(binding.etOpombe.getText().toString().trim());

        // Obvesti listener
        if (updateListener != null) {
            updateListener.onKemikalijaUpdated(kemikalija);
        }

        // Osveži prikaz
        displayData();

        // Vrni na view mode
        toggleEditMode(false);

        Toast.makeText(context, "Spremembe shranjene!", Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (kemikalija.getRok_uporabe() != null) {
            calendar.setTime(kemikalija.getRok_uporabe());
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    kemikalija.setRok_uporabe(selectedDate.getTime());
                    binding.btnRokUporabe.setText(dateFormat.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}