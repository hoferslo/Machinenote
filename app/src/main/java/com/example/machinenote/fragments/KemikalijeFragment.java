package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.HandleQRCode;
import com.example.machinenote.Utility.KeyboardUtils;
import com.example.machinenote.Utility.MailHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.KemikalijeBottomSheetFragment;
import com.example.machinenote.databinding.FragmentKemikalijeBinding;
import com.example.machinenote.models.Kemikalija;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class KemikalijeFragment extends BaseFragment implements QRCodeScannerFragment.QRCodeScanCallback {

    FragmentKemikalijeBinding binding;
    Context context;
    ApiManager apiManager;
    Kemikalija kemikalija;

    public KemikalijeFragment() {
        // Required empty public constructor
    }

    public static KemikalijeFragment newInstance(Context context) {
        KemikalijeFragment fragment = new KemikalijeFragment();
        fragment.context = context;
        fragment.TAG = "Kemikalije"; // Update with appropriate tag from strings.xml
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentKemikalijeBinding.inflate(getLayoutInflater());
        apiManager = new ApiManager(context);

        // Search functionality
        binding.idOfDuty.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    try {
                        int id = Integer.parseInt(query);
                        fetchKemikalijaById(id);
                    } catch (NumberFormatException e) {
                        // Search by name if not a number
                        searchKemikalijaByName(query);
                    }
                    KeyboardUtils.hideKeyboard(context);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Filter/Sort button
        binding.filterSortToggleBtn.setOnClickListener(v -> {
            // Implement filter/sort functionality
            Toast.makeText(context, "Filter functionality - coming soon", Toast.LENGTH_SHORT).show();
        });

        // Stock adjustment buttons
        binding.minusBtn.setOnClickListener(v -> adjustStock(-parseStockValue()));
        binding.plusBtn.setOnClickListener(v -> adjustStock(parseStockValue()));

        // Cancel button
        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        // Show all data
        binding.allDataSv.setOnClickListener(v -> showKemikalijeBottomSheet());

        // QR Code scanner
        binding.scanQRBtn.setOnClickListener(v -> startQRCodeScanner());

        // Gmail button
        binding.sendGmail.setOnClickListener(v -> openGmail());

        // SDS Button
        binding.sdsButton.setOnClickListener(v -> {
            if (kemikalija != null) {
                // Open SDS document or link
                Toast.makeText(context, "Opening SDS for " + kemikalija.getIme_SLO(), Toast.LENGTH_SHORT).show();
                // Implement SDS opening logic here
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

    private int parseStockValue() {
        try {
            String valueStr = binding.stockChangeValue.getText().toString();
            return TextUtils.isEmpty(valueStr) ? 1 : Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void openGmail() {
        if (kemikalija != null) {
            String subject = "Kemikalija - obvestilo: " + kemikalija.getIme_SLO();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String rokUporabe = kemikalija.getRok_uporabe() != null ?
                    dateFormat.format(kemikalija.getRok_uporabe()) : "Ni podatka";

            String body = "Pozdravljeni!\n\n" +
                    "Informacije o kemikaliji:\n\n" +
                    "Ime (SLO): " + kemikalija.getIme_SLO() + "\n" +
                    "Ime (ENG): " + (kemikalija.getIme_ENG() != null ? kemikalija.getIme_ENG() : "Ni podatka") + "\n" +
                    "CAS številka: " + (kemikalija.getCas_stevilo() != null ? kemikalija.getCas_stevilo() : "Ni podatka") + "\n" +
                    "Formula: " + (kemikalija.getFormula() != null ? kemikalija.getFormula() : "Ni podatka") + "\n" +
                    "Firma: " + kemikalija.getFirma() + "\n" +
                    "Rok uporabe: " + rokUporabe + "\n" +
                    "Teža/količina: " + (kemikalija.getTeza() != null ? kemikalija.getTeza() : "Ni podatka") + "\n" +
                    "Lokacija: " + (kemikalija.getPolica() != null ? kemikalija.getPolica() : "Ni podatka") + "\n\n" +
                    "Lep pozdrav";

            MailHelper.openEmailClient(
                    context,
                    "matej.kandare@unichem.si",
                    subject,
                    body
            );
        } else {
            Toast.makeText(context, "Ni podatkov o kemikaliji", Toast.LENGTH_SHORT).show();
        }
    }

    private void startQRCodeScanner() {
        ((MainActivity) context).loadFragment(QRCodeScannerFragment.newInstance(context, this));
    }

    private void fetchKemikalijaById(int id) {
        // You'll need to add this method to ApiManager
        apiManager.fetchKemikalijaById(id, new ApiManager.KemikalijaByIdCallback() {
            @Override
            public void onSuccess(Kemikalija kemikalijaData) {
                kemikalija = kemikalijaData;
                updateUI();
            }

            @Override
            public void onFailure(String errorMessage) {
                System.err.println(getString(R.string.error) + errorMessage);
                Toast.makeText(context, "Napaka pri pridobivanju kemikalije", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchKemikalijaByName(String name) {
        // Implement search by name functionality
        Toast.makeText(context, "Searching for: " + name, Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        if (kemikalija == null) return;

        // Update chemical name
        binding.chemicalName.setText(kemikalija.getIme_SLO() != null ? kemikalija.getIme_SLO() : "");

        // Update CAS and purity info
        String casInfo = "";
        if (kemikalija.getCas_stevilo() != null) {
            casInfo = "CAS: " + kemikalija.getCas_stevilo();
        }
        if (kemikalija.getFirma() != null) {
            casInfo += (casInfo.isEmpty() ? "" : " | ") + kemikalija.getFirma();
        }
        binding.chemicalCasPurity.setText(casInfo);

        // Update location (Polica)
        binding.articleRack.setText(kemikalija.getPolica() != null ? kemikalija.getPolica() : "-");

        // Update expiry date (Rok uporabe)
        if (kemikalija.getRok_uporabe() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            binding.articleWarehouse.setText(dateFormat.format(kemikalija.getRok_uporabe()));

            // Check if expired and highlight
            if (kemikalija.getRok_uporabe().before(new Date())) {
                binding.articleWarehouse.setTextColor(context.getColor(R.color.red_600));
            } else {
                binding.articleWarehouse.setTextColor(context.getColor(R.color.content_primary));
            }
        } else {
            binding.articleWarehouse.setText("-");
        }

        // Update CAS number
        binding.articleId.setText(kemikalija.getCas_stevilo() != null ? kemikalija.getCas_stevilo() : "-");

        // Update formula
        binding.articleMinimumTv.setText(kemikalija.getFormula() != null ? kemikalija.getFormula() : "-");

        // Update stock (Teza)
        binding.zaloga.setText(kemikalija.getTeza() != null ? kemikalija.getTeza() : "0 g");

        // Update hazard info (can be extended based on your needs)
        String hazardText = "Agregatno stanje: " +
                (kemikalija.getAgregatno_stanje() != null ? kemikalija.getAgregatno_stanje() : "Ni podatka");
        binding.hazardInfo.setText(hazardText);

        // Hide/show Gmail button (can be based on your logic)
        binding.sendGmail.setVisibility(View.GONE);
    }

    private void showKemikalijeBottomSheet() {
        if (kemikalija != null) {
            KemikalijeBottomSheetFragment bottomSheet =
                    KemikalijeBottomSheetFragment.newInstance(getContext(), kemikalija);
            bottomSheet.show(getChildFragmentManager(), "KemikalijeBottomSheet");
        } else {
            Toast.makeText(getContext(), "Ni podatkov o kemikaliji", Toast.LENGTH_SHORT).show();
        }
    }

    private void adjustStock(int change) {
        if (kemikalija != null) {
            // You'll need to implement this in ApiManager for kemikalije
            ApiManager.StockAdjustmentCallback callback = new ApiManager.StockAdjustmentCallback() {
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    Toast.makeText(context, "Napaka pri prilagajanju zaloge", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (kemikalija != null) {
                        fetchKemikalijaById(kemikalija.getId());
                    }
                }
            };
            // apiManager.adjustKemikalijaStock(kemikalija.getId(), change, callback);
            Toast.makeText(context, "Stock adjustment: " + change, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Ni podatkov o kemikaliji", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQRCodeScanned(String qrCode) {
        requireActivity().runOnUiThread(() -> {
            int id = HandleQRCode.getKemikalijaIdFromQR(qrCode); // You may need to create this method
            if (id != 0) {
                fetchKemikalijaById(id);
            } else {
                Toast.makeText(context, getString(R.string.wrong_qr_code), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onScanCancelled() {
        Toast.makeText(getContext(), getString(R.string.scan_cancelled), Toast.LENGTH_LONG).show();
    }
}