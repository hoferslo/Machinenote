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
import com.example.machinenote.Utility.AnimationHelper;
import com.example.machinenote.Utility.HandleQRCode;
import com.example.machinenote.Utility.KeyboardUtils;
import com.example.machinenote.Utility.MailHelper;
import com.example.machinenote.Utility.ViewUtils;
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
        // Required empty public constructor
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

        // Inflate the layout for this fragment
        binding = FragmentKnjizenjeBinding.inflate(getLayoutInflater());
        apiManager = new ApiManager(context);

        binding.getArticelDataByIdBtn.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            String idStr = binding.idOfArticleEt.getText().toString();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(context, "Please enter an ID", Toast.LENGTH_SHORT).show();
                return;
            }
            int id = Integer.parseInt(idStr);
            fetchRezervniDeliById(id);

            KeyboardUtils.hideKeyboard(context);
        });

        binding.minusBtn.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            adjustStock(-Integer.parseInt(binding.stockChangeValue.getText().toString()));
        });
        binding.plusBtn.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            adjustStock(Integer.parseInt(binding.stockChangeValue.getText().toString()));
        });

        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });



        binding.articleNameLayout.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            startQRCodeScanner();
        });

        // Nastavi click listener
        binding.allDataSv.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            showRezervniDeliBottomSheet();
        });

        binding.scanQRBtn.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            startQRCodeScanner();
        });

        binding.sendGmail.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            openGmail();
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }

    private void openGmail() {
        if (rezervniDel != null) {
            // Sestavimo subject z imenom artikla
            String subject = "Rezervni del pod zalogo: " + rezervniDel.getArtikel();

            // Sestavimo body z vsemi potrebnimi informacijami
            String body = "Pozdravljeni!\n\n" +
                    "Opozarjam vas, da je rezervni del pod minimalno zalogo:\n\n" +
                    "Artikel: " + rezervniDel.getArtikel() + "\n" +
                    "ID Artikla: " + rezervniDel.getId() + "\n" +
                    "Trenutna zaloga: " + rezervniDel.getRealZalogo() + "\n" +
                    "Minimalna zaloga: " + rezervniDel.getMinimalna_zaloga() + "\n" +
                    "Dobavitelj: " + (rezervniDel.getDobavitelj() != null ? rezervniDel.getDobavitelj() : "Ni podatka") + "\n" +
                    "Regal: " + rezervniDel.getRegal() + "\n" +
                    "Skladišče: " + rezervniDel.getSkladišče() + "\n\n" +
                    "Prosim, poskrbite za dopolnitev zaloge.\n\n" +
                    "Lep pozdrav";

            MailHelper.openEmailClient(
                    context,                              // context
                    "matej.kandare@unichem.si",           // recipient
                    subject,                              // subject
                    body                                  // body
            );
        } else {
            Toast.makeText(context, "Ni podatkov o rezervnem delu", Toast.LENGTH_SHORT).show();
        }
    }

    private void startQRCodeScanner() {
        ((MainActivity) context).loadFragment(QRCodeScannerFragment.newInstance(context, this));
    }

    private void fetchRezervniDeliById(int id) {

        apiManager.fetchRezervniDeliById(id, new ApiManager.RezervniDeliByIdCallback() {
            @Override
            public void onSuccess(RezervniDel rezervniDeli) {
                rezervniDel = rezervniDeli;
                updateUI();
            }

            @Override
            public void onFailure(String errorMessage) {
                System.err.println(getString(R.string.error) + errorMessage);
                Toast.makeText(context, getString(R.string.error_with_getting_rezervni_del), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUI() {
        binding.articleName.setText(rezervniDel.getArtikel());
        binding.articleRack.setText(rezervniDel.getRegal());
        binding.articleId.setText(String.valueOf(rezervniDel.getId()));
        binding.articleWarehouse.setText(String.valueOf(rezervniDel.getSkladišče()));
        binding.articleMinimumTv.setText(String.valueOf(rezervniDel.getMinimalna_zaloga()));
        binding.articleStock.setText(String.valueOf(rezervniDel.getRealZalogo()));


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

    private void showRezervniDeliBottomSheet() {
        if (rezervniDel != null) {
            RezervniDeliBottomSheetFragment bottomSheet =
                    RezervniDeliBottomSheetFragment.newInstance(getContext(), rezervniDel);
            bottomSheet.show(getChildFragmentManager(), "RezervniDeliBottomSheet");
        } else {
            // Opcijsko: prikaži sporočilo, če ni podatkov
            Toast.makeText(getContext(), "Ni podatkov o delu", Toast.LENGTH_SHORT).show();
        }
    }


    private void adjustStock(int change) {
        // Define the callback for stock adjustment
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
            // Update the stock in the backend if needed
        } else {
            Toast.makeText(context, getString(R.string.error_with_getting_rezervni_del), Toast.LENGTH_SHORT).show();
        }
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

    private void showBottomSheet() {
        RezervniDeliBottomSheetFragment bottomSheetFragment =
                RezervniDeliBottomSheetFragment.newInstance(context, rezervniDel);
        bottomSheetFragment.show(getChildFragmentManager(), "RezervniDeliBottomSheet");
    }
}