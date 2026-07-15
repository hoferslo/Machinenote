package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
// Animation was here AnimationHelper.bounceClick(view);
import com.example.machinenote.Utility.FuzzySearchHelper;
import com.example.machinenote.Utility.HandleQRCode;
import com.example.machinenote.Utility.KeyboardUtils;
import com.example.machinenote.Utility.MailHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.customFragments.KemikalijeBottomSheetFragment;
import com.example.machinenote.databinding.FragmentKemikalijeBinding;
import com.example.machinenote.models.Kemikalija;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class KemikalijeFragment extends BaseFragment implements QRCodeScannerFragment.QRCodeScanCallback, KemikalijeBottomSheetFragment.OnKemikalijaUpdateListener {

    FragmentKemikalijeBinding binding;
    Context context;
    ApiManager apiManager;
    Kemikalija kemikalija;
    List<Kemikalija> allKemikalije = new ArrayList<>();
    List<Kemikalija> filteredKemikalije = new ArrayList<>();

    // Dropdown components
    private PopupWindow dropdownPopup;
    private ListView dropdownListView;
    private ArrayAdapter<String> dropdownAdapter;

    public KemikalijeFragment() {
        // Required empty public constructor
    }

    public static KemikalijeFragment newInstance(Context context) {
        KemikalijeFragment fragment = new KemikalijeFragment();
        fragment.context = context;
        fragment.TAG = "Kemikalije";
        return fragment;
    }

    @Override
    public void onKemikalijaUpdated(Kemikalija updatedKemikalija) {
        // Posodobi lokalno kemikalijo
        this.kemikalija = updatedKemikalija;

        // Posodobi UI
        updateUI();

        apiManager.updateKemikalija(updatedKemikalija, new ApiManager.KemikalijaUpdateCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Kemikalija uspešno posodobljena", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                String errorMessage = throwable.getMessage() != null ? throwable.getMessage() : "Neznana napaka";
                Toast.makeText(context, "Napaka pri shranjevanju: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("KemikalijeFragment", "Update failed: " + errorMessage);
            }
        });
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

        // Initialize dropdown
        initializeDropdown();

        // Fetch all chemicals on fragment creation
        fetchAllKemikalije();
        int searchSrcTextId = binding.idOfDuty.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        View searchEditText = binding.idOfDuty.findViewById(searchSrcTextId);

        if (searchEditText != null) {
            searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if (allKemikalije.isEmpty()) {
                            Toast.makeText(context, "Nalaganje kemikalij...", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Show all items when focused
                        filteredKemikalije.clear();
                        filteredKemikalije.addAll(allKemikalije);

                        updateDropdownAdapter();

                        // Dodaj delay za prikaz
                        binding.idOfDuty.post(new Runnable() {
                            @Override
                            public void run() {
                                showDropdown();
                            }
                        });
                    } else {
                        hideDropdown();
                    }
                }
            });
        }

        // Search functionality - Query text listener
        binding.idOfDuty.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    searchKemikalije(query);
                    updateDropdownAdapter();
                    showDropdown();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    searchKemikalije(newText);
                    updateDropdownAdapter();
                    showDropdown();
                } else {
                    filteredKemikalije.clear();
                    filteredKemikalije.addAll(allKemikalije);
                    updateDropdownAdapter();
                    showDropdown();
                }
                return true;
            }
        });

        // Filter/Sort button
        binding.filterSortToggleBtn.setOnClickListener(v -> {
            // Animation was here AnimationHelper.bounceClick(v);
            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        });

        // Stock adjustment buttons
        binding.minusBtn.setOnClickListener(v -> {
            // Animation was here AnimationHelper.bounceClick(v);
            adjustStock(-parseStockValue());
        });
        binding.plusBtn.setOnClickListener(v -> {
            // Animation was here AnimationHelper.bounceClick(v);
            adjustStock(parseStockValue());
        });

        // Cancel button
        binding.cancelBtn.setOnClickListener(view -> {
         // Animation was here AnimationHelper.bounceClick(view);
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
                Toast.makeText(context, "Opening SDS for " + kemikalija.getIme_SLO(), Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void initializeDropdown() {
        // Create ListView for dropdown
        dropdownListView = new ListView(context);
        dropdownListView.setBackgroundColor(context.getColor(android.R.color.white));
        dropdownListView.setDividerHeight(1);

        // Create adapter
        dropdownAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new ArrayList<>());
        dropdownListView.setAdapter(dropdownAdapter);

        // Create PopupWindow
        dropdownPopup = new PopupWindow(
                dropdownListView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        dropdownPopup.setOutsideTouchable(true);
        dropdownPopup.setFocusable(false);
        dropdownPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        dropdownPopup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Handle item clicks
        dropdownListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < filteredKemikalije.size()) {
                kemikalija = filteredKemikalije.get(position);
                updateUI();

                // Clear search and hide dropdown
                binding.idOfDuty.setQuery("", false);
                binding.idOfDuty.clearFocus();
                hideDropdown();
            }
        });
    }

    private void updateDropdownAdapter() {
        Log.d("KemikalijeFragment", "Updating dropdown adapter with " + filteredKemikalije.size() + " items");
        List<String> displayNames = new ArrayList<>();
        for (Kemikalija k : filteredKemikalije) {
            String name = "";
            if (k.getIme_SLO() == null || Objects.equals(k.getIme_SLO(), "")){
                name = k.getIme_ENG() != null ? k.getIme_ENG() : "Neznano";
            } else {
                name = k.getIme_SLO() != null ? k.getIme_SLO() : "Neznano";
            }
            String cas = k.getCas_stevilo() != null ? " (CAS: " + k.getCas_stevilo() + ")" : "";
            String formula = k.getFormula() != null ? " - " + k.getFormula() : "";
            displayNames.add(name + cas + formula);
        }

        dropdownAdapter.clear();
        dropdownAdapter.addAll(displayNames);
        dropdownAdapter.notifyDataSetChanged();
    }

    private void showDropdown() {
        // Update adapter with filtered results
        updateDropdownAdapter();

        // Calculate max height (show max 5 items)
        int itemHeight = 120;
        int maxHeight = Math.min(filteredKemikalije.size(), 5) * itemHeight;
        dropdownPopup.setHeight(maxHeight);

        // Show popup below search view
        if (!dropdownPopup.isShowing()) {
            dropdownPopup.showAsDropDown(binding.idOfDuty, 0, 0);
        } else {
            dropdownPopup.update(binding.idOfDuty, 0, 0,
                    ViewGroup.LayoutParams.MATCH_PARENT, maxHeight);
        }
    }

    private void hideDropdown() {
        if (dropdownPopup != null && dropdownPopup.isShowing()) {
            dropdownPopup.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideDropdown();
    }

    private void fetchAllKemikalije() {
        apiManager.fetchAllKemikalije(new ApiManager.KemikalijeListCallback() {
            @Override
            public void onSuccess(List<Kemikalija> kemikalije) {
                allKemikalije = kemikalije;
                filteredKemikalije.clear();
                filteredKemikalije.addAll(allKemikalije);;

                // Če je search bar fokusiran, prikaži dropdown
                int searchSrcTextId = binding.idOfDuty.getContext().getResources()
                        .getIdentifier("android:id/search_src_text", null, null);
                View searchEditText = binding.idOfDuty.findViewById(searchSrcTextId);

                if (searchEditText != null && searchEditText.hasFocus()) {
                    updateDropdownAdapter();
                    binding.idOfDuty.post(new Runnable() {
                        @Override
                        public void run() {
                            showDropdown();
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                System.err.println(getString(R.string.error) + errorMessage);
                Toast.makeText(context, "Napaka pri pridobivanju kemikalij", Toast.LENGTH_SHORT).show();
                Log.e("KemikalijeFragment", "Error loading chemicals: " + errorMessage);
            }
        });
    }

    private void searchKemikalije(String query) {
        // FuzzySearchHelper samodejno obravnava null/prazen query in vrne celoten seznam
        filteredKemikalije = FuzzySearchHelper.search(
                allKemikalije,
                query,
                Kemikalija::getIme_SLO,
                Kemikalija::getIme_ENG,
                Kemikalija::getCas_stevilo,
                Kemikalija::getFormula
        );
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

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
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

            MailHelper.openEmailClient(context, "matej.kandare@unichem.si", subject, body);
        } else {
            Toast.makeText(context, "Ni podatkov o kemikaliji", Toast.LENGTH_SHORT).show();
        }
    }

    private void startQRCodeScanner() {
        ((MainActivity) context).loadFragment(QRCodeScannerFragment.newInstance(context, this));
    }

    private void fetchKemikalijaById(int id) {
        apiManager.fetchKemikalijaId(new ApiManager.KemikalijaIdCallback() {
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
        }, id);
    }

    private void updateUI() {
        if (kemikalija == null) return;

        binding.chemicalName.setText(kemikalija.getIme_SLO() != null ? kemikalija.getIme_SLO() : "");

        String casInfo = "";
        if (kemikalija.getCas_stevilo() != null) {
            casInfo = "CAS: " + kemikalija.getCas_stevilo();
        }
        if (kemikalija.getFirma() != null) {
            casInfo += (casInfo.isEmpty() ? "" : " | ") + kemikalija.getFirma();
        }
        binding.chemicalCasPurity.setText(casInfo);

        binding.articleRack.setText(kemikalija.getPolica() != null ? kemikalija.getPolica() : "-");

        if (kemikalija.getRok_uporabe() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            binding.articleWarehouse.setText(dateFormat.format(kemikalija.getRok_uporabe()));

            if (kemikalija.getRok_uporabe().before(new Date())) {
                binding.articleWarehouse.setTextColor(context.getColor(R.color.red_600));
            } else {
                binding.articleWarehouse.setTextColor(context.getColor(R.color.content_primary));
            }
        } else {
            binding.articleWarehouse.setText("-");

        }

        binding.articleId.setText(kemikalija.getCas_stevilo() != null ? kemikalija.getCas_stevilo() : "-");
        binding.articleMinimumTv.setText(kemikalija.getFormula() != null ? kemikalija.getFormula() : "-");
        binding.zaloga.setText(kemikalija.getTeza() != null ? kemikalija.getTeza() : "0 g");

        String hazardText = "Agregatno stanje: " +
                (kemikalija.getAgregatno_stanje() != null ? kemikalija.getAgregatno_stanje() : "Ni podatka");
        binding.hazardInfo.setText(hazardText);

        binding.sendGmail.setVisibility(View.GONE);
    }

    private void showKemikalijeBottomSheet() {
        if (kemikalija != null) {
            KemikalijeBottomSheetFragment bottomSheet =
                    KemikalijeBottomSheetFragment.newInstance(getContext(), kemikalija, this);
            bottomSheet.show(getChildFragmentManager(), "KemikalijeBottomSheet");
        } else {
            Toast.makeText(getContext(), "Ni podatkov o kemikaliji", Toast.LENGTH_SHORT).show();
        }
    }

    private void adjustStock(int change) {
        if (kemikalija != null) {
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
        } else {
            Toast.makeText(context, "Ni podatkov o kemikaliji", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onQRCodeScanned(String qrCode) {
        requireActivity().runOnUiThread(() -> {
            int id = HandleQRCode.getKemikalijaIdFromQR(qrCode);
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