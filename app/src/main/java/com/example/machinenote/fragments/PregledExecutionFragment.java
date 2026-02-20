package com.example.machinenote.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.CustomDatePicker;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentPregledExecutionBinding;
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.models.Linija;
import com.google.gson.JsonObject;
import com.example.machinenote.models.PregledOpravilo;

import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PregledExecutionFragment extends BaseFragment {

    private FragmentPregledExecutionBinding binding;
    private ApiManager apiManager;
    private PreventivniPregled selectedPregled;
    private Linija selectedLinija;
    private String maintainerName;
    private SharedPreferencesHelper sharedPreferencesHelper;


    public PregledExecutionFragment() {}

    public static PregledExecutionFragment newInstance(Context context, PreventivniPregled pregled, Linija linija) {
        PregledExecutionFragment fragment = new PregledExecutionFragment();
        fragment.apiManager = new ApiManager(context);
        fragment.selectedPregled = pregled;
        fragment.selectedLinija = linija;
        fragment.TAG = "Izvajanje pregleda - " + (pregled != null ? pregled.getId() : "");
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPregledExecutionBinding.inflate(inflater, container, false);
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());
        setupUI();
        setupEventListeners();
        loadPregledInfo();

        return binding.getRoot();
    }

    private void setupUI() {
        // Set default value for inspection type
        binding.etTipPregleda.setText("Redni pregled");

        // Set current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        binding.etDatum.setText(currentDate);
        maintainerName = sharedPreferencesHelper.getUsername();
        if (maintainerName != null && !maintainerName.isEmpty()) {
            binding.etVzdrzevalec.setText(maintainerName);
        }
        // Get maintainer name from shared preferences or user input
    }

    private void setupEventListeners() {
        binding.btnExecutePregled.setOnClickListener(v -> executePregled());

        binding.btnCancel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Nastavi date picker za datum polje
        setupDatePicker();

        // Set up tab navigation if needed
        setupTabNavigation();
    }

    private void setupTabNavigation() {
        binding.switchTabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                if (checkedId == R.id.btnCancel) {
                    mainActivity.getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void loadPregledInfo() {
        if (selectedPregled == null) {
            Toast.makeText(getContext(), "Napaka: pregled ni izbran", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        // Set toolbar title
        String title = selectedPregled.getOpis() != null && !selectedPregled.getOpis().isEmpty() ?
                selectedPregled.getOpis() :
                ("Pregled ID: " + selectedPregled.getId());
        ((MainActivity) requireActivity()).binding.toolbarTitle.setText("Izvajanje: " + title);

        // Display pregled information
        binding.tvPregledInfo.setText(buildPregledInfoText());

        // Pre-fill some fields if available
    }

    private void setupDatePicker() {
        binding.etDatum.setOnClickListener(v -> {
            CustomDatePicker datePicker = new CustomDatePicker(requireContext(),
                    new CustomDatePicker.ICustomDateListener() {
                        @Override
                        public void onSet(Dialog dialog, Calendar calendarSelected,
                                          Date dateSelected, int year, String monthFullName,
                                          String monthShortName, int monthNumber, int day,
                                          String weekDayFullName, String weekDayShortName) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String dateString = dateFormat.format(dateSelected);
                            binding.etDatum.setText(dateString);
                        }

                        @Override
                        public void onCancel() {
                            // Nič ne naredi
                        }
                    });

            // Nastavi trenutni datum iz polja
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date currentDate = dateFormat.parse(binding.etDatum.getText().toString());
                if (currentDate != null) {
                    datePicker.setDate(currentDate);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
                datePicker.setDate(Calendar.getInstance());
            }

            datePicker.showDialog();
        });
    }

    private String buildPregledInfoText() {
        StringBuilder info = new StringBuilder();

        if (selectedLinija != null) {
            info.append("Linija: ").append(selectedLinija.getLinija_SAP())
                    .append(" - ").append(selectedLinija.getNaziv_linije()).append("\n");
        }

        // Dodaj podsklop, če obstaja
        if (selectedPregled.getPodsklopLinije() != null && !selectedPregled.getPodsklopLinije().isEmpty()) {
            info.append("Podsklop: ").append(selectedPregled.getNazivPodsklopa()).append("\n");
        }

        info.append("Opis: ").append(selectedPregled.getOpis() != null ? selectedPregled.getOpis() : "Ni opisa").append("\n");

        if (selectedPregled.getFullLocation() != null) {
            info.append("Lokacija: ").append(selectedPregled.getFullLocation()).append("\n");
        }

        if (selectedPregled.getFrekvenca() > 0) {
            info.append("Frekvenca: ").append(selectedPregled.getFrekvenca()).append(" dni\n");
        }

        if (selectedPregled.getLastnost() != null && !selectedPregled.getLastnost().isEmpty()) {
            info.append("Lastnost: ").append(selectedPregled.getLastnost()).append("\n");
        }

        if (selectedPregled.getStdVrednost() != null && !selectedPregled.getStdVrednost().isEmpty()) {
            info.append("Standardna vrednost: ").append(selectedPregled.getStdVrednost()).append("\n");
        }

        if (selectedPregled.getOpombe() != null && !selectedPregled.getOpombe().isEmpty()) {
            info.append("Opombe: ").append(selectedPregled.getOpombe());
        }

        return info.toString();
    }

    private void executePregled() {
        if (!validateInput()) {
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Potrditev izvajanja")
                .setMessage("Ali ste prepričani, da želite izvesti ta pregled?")
                .setPositiveButton("Da", (dialog, which) -> {
                    performExecution();
                })
                .setNegativeButton("Ne", null)
                .show();
    }

    private boolean validateInput() {
        String trajanje = binding.etTrajanjeMin.getText().toString().trim();
        String vzdrzevalec = binding.etVzdrzevalec.getText().toString().trim();
        String tipPregleda = binding.etTipPregleda.getText().toString().trim();

        if (trajanje.isEmpty()) {
            binding.etTrajanjeMin.setError("Vnesite trajanje v minutah");
            binding.etTrajanjeMin.requestFocus();
            return false;
        }

        try {
            int trajanjeInt = Integer.parseInt(trajanje);
            if (trajanjeInt < 0) {
                binding.etTrajanjeMin.setError("Trajanje ne more biti negativno");
                binding.etTrajanjeMin.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            binding.etTrajanjeMin.setError("Vnesite veljavno število");
            binding.etTrajanjeMin.requestFocus();
            return false;
        }

        if (vzdrzevalec.isEmpty()) {
            binding.etVzdrzevalec.setError("Vnesite ime vzdrževalca");
            binding.etVzdrzevalec.requestFocus();
            return false;
        }

        if (tipPregleda.isEmpty()) {
            binding.etTipPregleda.setError("Vnesite tip pregleda");
            binding.etTipPregleda.requestFocus();
            return false;
        }

        return true;
    }

    private void performExecution() {
        MainActivity mainActivity = (MainActivity) requireActivity();

        if (!mainActivity.serverConnection) {
            Toast.makeText(getContext(), "Ni povezave s strežnikom", Toast.LENGTH_LONG).show();
            return;
        }

        // Show loading
        binding.btnExecutePregled.setEnabled(false);
        binding.btnExecutePregled.setText("Izvajam...");

        // Pripravite podatke iz UI
        int trajanje = Integer.parseInt(binding.etTrajanjeMin.getText().toString().trim());
        String vzdrzevalec = binding.etVzdrzevalec.getText().toString().trim();
        String datum = binding.etDatum.getText().toString().trim();

        // Pridobite opombe in dejanske vrednosti iz UI
        String opombe = "";
        String dejanskaVrednost = "";

        // Če imate ta polja v vaše binding:
        if (binding.etOpombe != null) {
            opombe = binding.etOpombe.getText().toString().trim();
        }
        if (binding.etDejanskavrednost != null) {
            dejanskaVrednost = binding.etDejanskavrednost.getText().toString().trim();
        }

        // Pridobi podsklop_linije_id če obstaja
        Integer podsklopLinijeId = null;
        if (selectedPregled.getPodsklopLinijeId() != 0) {
            podsklopLinijeId = selectedPregled.getPodsklopLinijeId();
        }

        Log.d(TAG, "Sending data: opombe='" + opombe + "', dejanskaVrednost='" + dejanskaVrednost +
                "', podsklopLinijeId=" + podsklopLinijeId);

        // Call API z dodanim podsklopLinijeId parametrom
        apiManager.executePreventivniPregled(selectedPregled, trajanje, vzdrzevalec,
                datum, opombe, dejanskaVrednost, podsklopLinijeId, new ApiManager.PregledExecutionCallback() {
                    @Override
                    public void onSuccess(List<PregledOpravilo> response) {
                        if (getActivity() == null) return;

                        requireActivity().runOnUiThread(() -> {
                            binding.btnExecutePregled.setEnabled(true);
                            binding.btnExecutePregled.setText("Izvedi pregled");

                            String message = "Pregled uspešno izveden!";
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Pregled executed successfully");

                            // Navigate back
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (getActivity() == null) return;

                        requireActivity().runOnUiThread(() -> {
                            binding.btnExecutePregled.setEnabled(true);
                            binding.btnExecutePregled.setText("Izvedi pregled");

                            Toast.makeText(getContext(), "Napaka pri izvajanju: " + errorMessage, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Pregled execution failed: " + errorMessage);
                        });
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedPregled != null) {
            String title = selectedPregled.getOpis() != null && !selectedPregled.getOpis().isEmpty() ?
                    selectedPregled.getOpis() : ("Pregled ID: " + selectedPregled.getId());
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.binding.toolbarTitle.setText("Izvajanje: " + title);
            mainActivity.showBackArrow();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}