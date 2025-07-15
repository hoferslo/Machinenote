package com.example.machinenote.customFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.R;
import com.example.machinenote.databinding.FragmentRezervniDeliBottomSheetBinding;
import com.example.machinenote.models.DrobniMateriali;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DrobniMaterialiBottomSheetFragment extends BottomSheetDialogFragment {

    public FragmentRezervniDeliBottomSheetBinding binding;
    private Context context;
    private DrobniMateriali drobniMaterial;

    public static DrobniMaterialiBottomSheetFragment newInstance(Context context, DrobniMateriali drobniMaterial) {
        DrobniMaterialiBottomSheetFragment fragment = new DrobniMaterialiBottomSheetFragment();
        fragment.drobniMaterial = drobniMaterial;
        fragment.context = context;
        return fragment;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentRezervniDeliBottomSheetBinding.inflate(inflater, container, false);

        // Populate header with material name instead of ID
        binding.partIdHeader.setText(getString(R.string.material_header, drobniMaterial.getMaterial()));

        // Populate location data
        binding.warehouse.setText(String.valueOf(drobniMaterial.getSkladišče()));
        if (drobniMaterial.getRegal() != null && !drobniMaterial.getRegal().isEmpty()) {
            binding.shelf.setText(drobniMaterial.getRegal());
        }

        // Populate material data (use article fields for material info)
        if (drobniMaterial.getVrsta() != null && !drobniMaterial.getVrsta().isEmpty()) {
            binding.articleName.setText(drobniMaterial.getVrsta());
        }
        if (drobniMaterial.getVelikost() != null && !drobniMaterial.getVelikost().isEmpty()) {
            binding.longDescription.setText(getString(R.string.velikost_label, drobniMaterial.getVelikost()));
        }

        // Show quality if available
        if (drobniMaterial.getKvaliteta() != null && !drobniMaterial.getKvaliteta().isEmpty()) {
            binding.manufacturer.setText(getString(R.string.kvaliteta_label, drobniMaterial.getKvaliteta()));
        } else {
            binding.manufacturer.setVisibility(View.GONE);
        }

        // Hide elements that don't apply to DrobniMateriali
        hideUnusedElements();

        // Setup listeners for potential actions
        setupClickListeners();

        return binding.getRoot();
    }

    private void hideUnusedElements() {
        // Hide pricing section (not applicable for DrobniMateriali)
        binding.pricing.setVisibility(View.GONE);

        // Hide stockManagement
        binding.stockManagement.setVisibility(View.GONE);

        // Hide stock management fields (not applicable for DrobniMateriali)
        binding.partnerji.setVisibility(View.GONE);

        // You might also want to hide the labels/headers for these sections
        // Depending on your layout structure, you may need to hide parent containers
    }

    private void setupClickListeners() {
        // Click on warehouse/shelf could open location details
        binding.warehouse.setOnClickListener(v -> {
            Toast.makeText(context, getString(R.string.skladisce) + ": " + drobniMaterial.getSkladišče(), Toast.LENGTH_SHORT).show();
        });

        binding.shelf.setOnClickListener(v -> {
            if (drobniMaterial.getRegal() != null && !drobniMaterial.getRegal().isEmpty()) {
                Toast.makeText(context, getString(R.string.regal) + ": " + drobniMaterial.getRegal(), Toast.LENGTH_SHORT).show();
            }
        });

        // Click on material type
        binding.articleName.setOnClickListener(v -> {
            if (drobniMaterial.getVrsta() != null && !drobniMaterial.getVrsta().isEmpty()) {
                Toast.makeText(context, getString(R.string.vrsta) + ": " + drobniMaterial.getVrsta(), Toast.LENGTH_SHORT).show();
            }
        });

        // Click on quality
        binding.manufacturer.setOnClickListener(v -> {
            if (drobniMaterial.getKvaliteta() != null && !drobniMaterial.getKvaliteta().isEmpty()) {
                Toast.makeText(context, getString(R.string.kvaliteta) + ": " + drobniMaterial.getKvaliteta(), Toast.LENGTH_SHORT).show();
            }
        });

        // Long click on size could show more details
        binding.longDescription.setOnLongClickListener(v -> {
            if (drobniMaterial.getVelikost() != null && !drobniMaterial.getVelikost().isEmpty()) {
                String info = getString(R.string.material_info,
                        drobniMaterial.getMaterial(),
                        drobniMaterial.getVrsta(),
                        drobniMaterial.getVelikost());
                Toast.makeText(context, info, Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up binding when the view is destroyed to prevent memory leaks
        binding = null;
    }
}