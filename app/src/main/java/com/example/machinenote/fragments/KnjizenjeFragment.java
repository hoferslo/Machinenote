package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.machinenote.customFragments.RezervniDeliBottomSheetFragment;
import com.example.machinenote.databinding.FragmentKnjizenjeBinding;
import com.example.machinenote.models.RezervniDel;

import retrofit2.Call;
import retrofit2.Response;


public class KnjizenjeFragment extends BaseFragment implements QRCodeScannerFragment.QRCodeScanCallback {

    FragmentKnjizenjeBinding binding;
    Context context;
    ApiManager apiManager;
    RezervniDel rezervniDel;

    public KnjizenjeFragment() {
    }

    public static KnjizenjeFragment newInstance(Context context) {
        KnjizenjeFragment fragment = new KnjizenjeFragment();
        fragment.context = context;
        fragment.TAG = context.getString(R.string.tag_knjizenje);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentKnjizenjeBinding.inflate(getLayoutInflater());
        apiManager = new ApiManager(context);

        binding.getArticelDataByIdBtn.setOnClickListener(v -> {
            String idStr = binding.idOfArticleEt.getText().toString();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(context, "Please enter an ID", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchRezervniDeliById(Integer.parseInt(idStr));
            KeyboardUtils.hideKeyboard(context);
        });

        binding.minusBtn.setOnClickListener(v -> {
            double change = parseStockChange();
            if (change != -1) adjustStock(-change);
        });

        binding.plusBtn.setOnClickListener(v -> {
            double change = parseStockChange();
            if (change != -1) adjustStock(change);
        });

        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        binding.articleNameLayout.setOnClickListener(v -> startQRCodeScanner());
        binding.allDataSv.setOnClickListener(v -> showRezervniDeliBottomSheet());
        binding.scanQRBtn.setOnClickListener(v -> startQRCodeScanner());
        binding.sendGmail.setOnClickListener(v -> openGmail());

        return binding.getRoot();
    }

    // ── Validacija vnosa ─────────────────────────────────────────────────────

    private double parseStockChange() {
        String val = binding.stockChangeValue.getText().toString();
        if (TextUtils.isEmpty(val)) {
            Toast.makeText(context, "Vnesite vrednost", Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (rezervniDel == null) {
            Toast.makeText(context, getString(R.string.error_with_getting_rezervni_del), Toast.LENGTH_SHORT).show();
            return -1;
        }

        double change = Double.parseDouble(val);

        // Če enota ne podpira decimalnih vrednosti, zavrni decimalni vnos
        if (!rezervniDel.isEnotaDecimal() && change != Math.floor(change)) {
            Toast.makeText(context, "Ta enota podpira samo cela števila", Toast.LENGTH_SHORT).show();
            return -1;
        }

        return change;
    }

    // ── UI ───────────────────────────────────────────────────────────────────

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }

    private void updateUI() {
        binding.articleName.setText(rezervniDel.getArtikel());
        binding.articleRack.setText(rezervniDel.getRegal());
        binding.articleId.setText(String.valueOf(rezervniDel.getId()));
        binding.articleWarehouse.setText(String.valueOf(rezervniDel.getSkladišče()));
        binding.articleMinimumTv.setText(rezervniDel.getMinimalna_zaloga() + " " + rezervniDel.getEnotaNaziv());
        binding.articleStock.setText(rezervniDel.getRealZalogo() + " " + rezervniDel.getEnotaNaziv());
        hideMail();

        if (rezervniDel.getRealZalogo() < rezervniDel.getMinimalna_zaloga()) {
            showMail();
        }
    }

    private void showMail() {
        binding.sendGmail.setVisibility(View.VISIBLE);
        binding.articleMinimumLl.setBackgroundColor(context.getColor(R.color.red_600));
    }

    private void hideMail() {
        binding.sendGmail.setVisibility(View.GONE);
        binding.articleMinimumLl.setBackgroundResource(R.drawable.bg_card_secondary);
    }

    // ── API ──────────────────────────────────────────────────────────────────

    private void fetchRezervniDeliById(int id) {
        apiManager.fetchRezervniDeliById(id, new ApiManager.RezervniDeliByIdCallback() {
            @Override
            public void onSuccess(RezervniDel rezervniDeli) {
                rezervniDel = rezervniDeli;
                updateUI();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, getString(R.string.error_with_getting_rezervni_del), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adjustStock(double change) {
        if (rezervniDel != null) {
            ApiManager.StockAdjustmentCallback callback = new ApiManager.StockAdjustmentCallback() {
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    Toast.makeText(context, getString(R.string.error_with_getting_rezervni_del), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (rezervniDel != null) {
                        fetchRezervniDeliById(rezervniDel.getId());
                    }
                }
            };
            apiManager.adjustStock(rezervniDel.getId(), change, callback);
        } else {
            Toast.makeText(context, getString(R.string.error_with_getting_rezervni_del), Toast.LENGTH_SHORT).show();
        }
    }

    // ── Gmail ────────────────────────────────────────────────────────────────

    private void openGmail() {
        if (rezervniDel != null) {
            String subject = "Rezervni del pod zalogo: " + rezervniDel.getArtikel();
            String body = "Pozdravljeni!\n\n" +
                    "Opozarjam vas, da je rezervni del pod minimalno zalogo:\n\n" +
                    "Artikel: " + rezervniDel.getArtikel() + "\n" +
                    "ID Artikla: " + rezervniDel.getId() + "\n" +
                    "Trenutna zaloga: " + rezervniDel.getRealZalogo() + " " + rezervniDel.getEnotaNaziv() + "\n" +
                    "Minimalna zaloga: " + rezervniDel.getMinimalna_zaloga() + " " + rezervniDel.getEnotaNaziv() + "\n" +
                    "Dobavitelj: " + (rezervniDel.getDobavitelj() != null ? rezervniDel.getDobavitelj() : "Ni podatka") + "\n" +
                    "Regal: " + rezervniDel.getRegal() + "\n" +
                    "Skladišče: " + rezervniDel.getSkladišče() + "\n\n" +
                    "Prosim, poskrbite za dopolnitev zaloge.\n\n" +
                    "Lep pozdrav";

            MailHelper.openEmailClient(context, "matej.kandare@unichem.si", subject, body);
        } else {
            Toast.makeText(context, "Ni podatkov o rezervnem delu", Toast.LENGTH_SHORT).show();
        }
    }

    // ── QR ───────────────────────────────────────────────────────────────────

    private void startQRCodeScanner() {
        ((MainActivity) context).loadFragment(QRCodeScannerFragment.newInstance(context, this));
    }

    @Override
    public void onQRCodeScanned(String qrCode) {
        requireActivity().runOnUiThread(() -> {
            int id = HandleQRCode.getRezervniDeliIdFromQR(qrCode);
            if (id != 0) {
                fetchRezervniDeliById(id);
            } else {
                Toast.makeText(context, getString(R.string.wrong_qr_code), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onScanCancelled() {
        Toast.makeText(getContext(), getString(R.string.scan_cancelled), Toast.LENGTH_LONG).show();
    }

    // ── Bottom Sheet ─────────────────────────────────────────────────────────

    private void showRezervniDeliBottomSheet() {
        if (rezervniDel != null) {
            RezervniDeliBottomSheetFragment bottomSheet =
                    RezervniDeliBottomSheetFragment.newInstance(getContext(), rezervniDel);
            bottomSheet.show(getChildFragmentManager(), "RezervniDeliBottomSheet");
        } else {
            Toast.makeText(getContext(), "Ni podatkov o delu", Toast.LENGTH_SHORT).show();
        }
    }
}