package com.example.machinenote.customFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.R;
import com.example.machinenote.databinding.FragmentRezervniDeliBottomSheetBinding;
import com.example.machinenote.models.RezervniDel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DecimalFormat;

public class RezervniDeliBottomSheetFragment extends BottomSheetDialogFragment {

    public FragmentRezervniDeliBottomSheetBinding binding;
    private Context context;
    private RezervniDel rezervniDel;
    private DecimalFormat currencyFormat;

    public static RezervniDeliBottomSheetFragment newInstance(Context context, RezervniDel rezervniDel) {
        RezervniDeliBottomSheetFragment fragment = new RezervniDeliBottomSheetFragment();
        fragment.rezervniDel = rezervniDel;
        fragment.context = context;
        return fragment;
    }

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentRezervniDeliBottomSheetBinding.inflate(inflater, container, false);

        // Initialize currency formatter
        currencyFormat = new DecimalFormat("#,##0.00 €");

        // Populate header with ID
        binding.partIdHeader.setText(getString(R.string.id_rezervnega_dela) + ": " + rezervniDel.getId());

        String enota = rezervniDel.getEnotaNaziv();
        //Log.d("WOW", "RezervniDel ID: " + getString(R.string.id_rezervnega_dela, rezervniDel.getId()));
        // Populate location data
        binding.warehouse.setText(String.valueOf(rezervniDel.getSkladišče()));
        if (rezervniDel.getRegal() != null && !rezervniDel.getRegal().isEmpty()) {
            binding.shelf.setText(rezervniDel.getRegal());
        }

        // Populate article data
        if (rezervniDel.getArtikel() != null && !rezervniDel.getArtikel().isEmpty()) {
            binding.articleName.setText(rezervniDel.getArtikel());
        }
        if (rezervniDel.getArtikel_dolgi_text() != null && !rezervniDel.getArtikel_dolgi_text().isEmpty()) {
            binding.longDescription.setText(rezervniDel.getArtikel_dolgi_text());
        }

        // Populate partner data
        if (rezervniDel.getProizvajalec() != null && !rezervniDel.getProizvajalec().isEmpty()) {
            binding.manufacturer.setText(rezervniDel.getProizvajalec());
        }
        if (rezervniDel.getDobavitelj() != null && !rezervniDel.getDobavitelj().isEmpty()) {
            binding.supplier.setText(rezervniDel.getDobavitelj());
        }

        // Populate pricing data
        if (rezervniDel.getZnesek() > 0) {
            binding.unitPrice.setText(currencyFormat.format(rezervniDel.getZnesek()));
        }
        // Calculate total price (unit price * real stock)
        double totalPrice = rezervniDel.getZnesek() * rezervniDel.getRealZalogo();
        if (totalPrice > 0) {
            binding.totalPrice.setText(currencyFormat.format(totalPrice));
        }

        // Populate stock data
        if (rezervniDel.getMinimalna_zaloga() > 0) {
            binding.minStock.setText(rezervniDel.getMinimalna_zaloga() + " " + enota);
        }
        if (rezervniDel.getDobava() > 0) {
            binding.supply.setText(rezervniDel.getDobava() + " " + enota);
        }
        if (rezervniDel.getPoraba() > 0) {
            binding.consumption.setText(rezervniDel.getPoraba() + " " + enota);
        }
        if (rezervniDel.getInventura() > 0) {
            binding.inventory.setText(String.valueOf(rezervniDel.getInventura()));
        }

        // Populate actual stock (real stock) with color coding
        double realStock = rezervniDel.getRealZalogo();

        binding.actualStock.setText(String.valueOf(realStock) + " " + enota);

        // Color code based on minimum stock
        if (realStock < rezervniDel.getMinimalna_zaloga()) {
            // Low stock - red color
            binding.actualStock.setTextColor(getResources().getColor(R.color.error_primary, null));
        } else if (realStock < rezervniDel.getMinimalna_zaloga() * 1.5) {
            // Warning stock - orange color
            binding.actualStock.setTextColor(getResources().getColor(R.color.warning_primary, null));
        } else {
            // Good stock - green color
            binding.actualStock.setTextColor(getResources().getColor(R.color.success_primary, null));
        }

        // Setup listeners for potential actions
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        // Optional: Add click listeners for various actions

        // Click on warehouse/shelf could open location details
        binding.warehouse.setOnClickListener(v -> {
            Toast.makeText(context, getString(R.string.skladisce) + ": " + rezervniDel.getSkladišče(), Toast.LENGTH_SHORT).show();
        });

        binding.shelf.setOnClickListener(v -> {
            if (rezervniDel.getRegal() != null && !rezervniDel.getRegal().isEmpty()) {
                Toast.makeText(context, getString(R.string.regal) + ": " + rezervniDel.getRegal(), Toast.LENGTH_SHORT).show();
            }
        });

        // Click on manufacturer could open manufacturer details
        binding.manufacturer.setOnClickListener(v -> {
            if (rezervniDel.getProizvajalec() != null && !rezervniDel.getProizvajalec().isEmpty()) {
                Toast.makeText(context, getString(R.string.proizvajalec) + ": " + rezervniDel.getProizvajalec(), Toast.LENGTH_SHORT).show();
            }
        });

        // Click on supplier could open supplier details
        binding.supplier.setOnClickListener(v -> {
            if (rezervniDel.getDobavitelj() != null && !rezervniDel.getDobavitelj().isEmpty()) {
                Toast.makeText(context, getString(R.string.dobavitelj) + ": " + rezervniDel.getDobavitelj(), Toast.LENGTH_SHORT).show();
            }
        });

        // Click on actual stock could show stock history or allow editing
        binding.actualStock.setOnClickListener(v -> {
            double realStock = rezervniDel.getRealZalogo();
            String stockInfo = getString(R.string.dejanska_zaloga) + ": " + realStock;
            if (realStock < rezervniDel.getMinimalna_zaloga()) {
                stockInfo += "\n" + getString(R.string.nizka_zaloga_opozorilo);
            }
            Toast.makeText(context, stockInfo, Toast.LENGTH_LONG).show();
        });

        // Long click on description could show full text
        binding.longDescription.setOnLongClickListener(v -> {
            if (rezervniDel.getArtikel_dolgi_text() != null && !rezervniDel.getArtikel_dolgi_text().isEmpty()) {
                // Could open a dialog with full description
                Toast.makeText(context, rezervniDel.getArtikel_dolgi_text(), Toast.LENGTH_LONG).show();
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