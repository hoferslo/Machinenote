package com.example.machinenote.customFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.R;
import com.example.machinenote.databinding.FragmentKemikalijeBottomSheetBinding;
import com.example.machinenote.models.Kemikalija;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class KemikalijeBottomSheetFragment extends BottomSheetDialogFragment {

    public FragmentKemikalijeBottomSheetBinding binding;
    private Context context;
    private Kemikalija kemikalija;
    private SimpleDateFormat dateFormat;

    public static KemikalijeBottomSheetFragment newInstance(Context context, Kemikalija kemikalija) {
        KemikalijeBottomSheetFragment fragment = new KemikalijeBottomSheetFragment();
        fragment.kemikalija = kemikalija;
        fragment.context = context;
        return fragment;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentKemikalijeBottomSheetBinding.inflate(inflater, container, false);

        // Initialize date formatter
        dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        // Populate header with ID (if available)
        binding.chemicalIdHeader.setText("Podrobnosti o kemikaliji");

        // Populate basic information
        if (kemikalija.getIme_SLO() != null && !kemikalija.getIme_SLO().isEmpty()) {
            binding.tvImeSlo.setText("Slovensko ime: " + kemikalija.getIme_SLO());
        } else {
            binding.tvImeSlo.setText("Slovensko ime: Ni podatka");
        }

        if (kemikalija.getIme_ENG() != null && !kemikalija.getIme_ENG().isEmpty()) {
            binding.tvImeEng.setText("Angleško ime: " + kemikalija.getIme_ENG());
        } else {
            binding.tvImeEng.setText("Angleško ime: Ni podatka");
        }

        if (kemikalija.getFormula() != null && !kemikalija.getFormula().isEmpty()) {
            binding.tvFormula.setText("Formula: " + kemikalija.getFormula());
        } else {
            binding.tvFormula.setText("Formula: Ni podatka");
        }

        if (kemikalija.getCas_stevilo() != null && !kemikalija.getCas_stevilo().isEmpty()) {
            binding.tvCas.setText("CAS številka: " + kemikalija.getCas_stevilo());
        } else {
            binding.tvCas.setText("CAS številka: Ni podatka");
        }

        // Populate company and storage info
        if (kemikalija.getFirma() != null && !kemikalija.getFirma().isEmpty()) {
            binding.tvFirma.setText("Firma: " + kemikalija.getFirma());
        } else {
            binding.tvFirma.setText("Firma: Ni podatka");
        }

        if (kemikalija.getRok_uporabe() != null) {
            binding.tvRokUporabe.setText("Rok uporabe: " + dateFormat.format(kemikalija.getRok_uporabe()));
        } else {
            binding.tvRokUporabe.setText("Rok uporabe: Ni podatka");
        }

        if (kemikalija.getTeza() != null && !kemikalija.getTeza().isEmpty()) {
            binding.tvTeza.setText("Teža: " + kemikalija.getTeza());
        } else {
            binding.tvTeza.setText("Teža: Ni podatka");
        }

        if (kemikalija.getAgregatno_stanje() != null && !kemikalija.getAgregatno_stanje().isEmpty()) {
            binding.tvAgregatno.setText("Agregatno stanje: " + kemikalija.getAgregatno_stanje());
        } else {
            binding.tvAgregatno.setText("Agregatno stanje: Ni podatka");
        }

        // Populate location information
        if (kemikalija.getPolica() != null && !kemikalija.getPolica().isEmpty()) {
            binding.tvPolica.setText("Polica: " + kemikalija.getPolica());
        } else {
            binding.tvPolica.setText("Polica: Ni podatka");
        }

        if (kemikalija.getOmara() != null && !kemikalija.getOmara().isEmpty()) {
            binding.tvOmara.setText("Omara: " + kemikalija.getOmara());
        } else {
            binding.tvOmara.setText("Omara: Ni podatka");
        }

        if (kemikalija.getProgram() != null && !kemikalija.getProgram().isEmpty()) {
            binding.tvProgram.setText("Program: " + kemikalija.getProgram());
        } else {
            binding.tvProgram.setText("Program: Ni podatka");
        }

        // Populate additional info
        if (kemikalija.getIdent_Unichem() > 0) {
            binding.tvIdentUnichem.setText("Ident Unichem: " + kemikalija.getIdent_Unichem());
        } else {
            binding.tvIdentUnichem.setText("Ident Unichem: Ni podatka");
        }

        if (kemikalija.getOpombe() != null && !kemikalija.getOpombe().isEmpty()) {
            binding.tvOpombe.setText("Opombe: " + kemikalija.getOpombe());
        } else {
            binding.tvOpombe.setText("Opombe: Ni podatka");
        }

        // Setup listeners for potential actions
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        // Optional: Add click listeners for various actions

        // Click on location could show location details
        binding.tvPolica.setOnClickListener(v -> {
            if (kemikalija.getPolica() != null && !kemikalija.getPolica().isEmpty()) {
                Toast.makeText(context, "Polica: " + kemikalija.getPolica(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.tvOmara.setOnClickListener(v -> {
            if (kemikalija.getOmara() != null && !kemikalija.getOmara().isEmpty()) {
                Toast.makeText(context, "Omara: " + kemikalija.getOmara(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.tvProgram.setOnClickListener(v -> {
            if (kemikalija.getProgram() != null && !kemikalija.getProgram().isEmpty()) {
                Toast.makeText(context, "Program: " + kemikalija.getProgram(), Toast.LENGTH_SHORT).show();
            }
        });

        // Click on firma could show company details
        binding.tvFirma.setOnClickListener(v -> {
            if (kemikalija.getFirma() != null && !kemikalija.getFirma().isEmpty()) {
                Toast.makeText(context, "Firma: " + kemikalija.getFirma(), Toast.LENGTH_SHORT).show();
            }
        });

        // Long click on opombe could show full text
        binding.tvOpombe.setOnLongClickListener(v -> {
            if (kemikalija.getOpombe() != null && !kemikalija.getOpombe().isEmpty()) {
                Toast.makeText(context, kemikalija.getOpombe(), Toast.LENGTH_LONG).show();
            }
            return true;
        });

        // Click on CAS number could potentially search for MSDS
        binding.tvCas.setOnClickListener(v -> {
            if (kemikalija.getCas_stevilo() != null && !kemikalija.getCas_stevilo().isEmpty()) {
                Toast.makeText(context, "CAS številka: " + kemikalija.getCas_stevilo(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up binding when the view is destroyed to prevent memory leaks
        binding = null;
    }
}