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
import com.example.machinenote.Utility.ViewUtils;
import com.example.machinenote.activities.MainActivity;
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
            String idStr = binding.idOfArticleEt.getText().toString();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(context, "Please enter an ID", Toast.LENGTH_SHORT).show();
                return;
            }
            int id = Integer.parseInt(idStr);
            fetchRezervniDeliById(id);

            KeyboardUtils.hideKeyboard(context);
        });

        binding.minusBtn.setOnClickListener(v -> adjustStock(-Integer.parseInt(binding.stockChangeValue.getText().toString())));
        binding.plusBtn.setOnClickListener(v -> adjustStock(Integer.parseInt(binding.stockChangeValue.getText().toString())));

        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        binding.scanQRBtn.setOnClickListener(v -> startQRCodeScanner());

        binding.sendGmail.setOnClickListener(v -> openGmail());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }

    private void openGmail() {
        MailHelper.openEmailClient(
                context,                              // context
                "casperkadivec@gmail.com",             // recipient
                "Subject of the email",            // subject
                "Body of the email"                // body
        );
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
        binding.articleWarehouse.setText(String.valueOf(rezervniDel.getSkladišče()));
        binding.articleMinimumTv.setText(String.valueOf(rezervniDel.getMinimalna_zaloga()));
        binding.articleStock.setText(String.valueOf(rezervniDel.getRealZalogo()));

        addToDataLl();

        hideMail();

        if (rezervniDel.getRealZalogo() < rezervniDel.getMinimalna_zaloga()) {
            showMail();
        }
    }

    private void showMail() {
        binding.sendGmail.setVisibility(View.VISIBLE);
        binding.articleMinimumLl.setBackgroundColor(context.getColor(R.color.red));
    }

    private void hideMail() {
        binding.sendGmail.setVisibility(View.GONE);
        binding.articleMinimumLl.setBackgroundResource(R.drawable.background_corners_50);
    }

    private void addToDataLl() {
        binding.allDataSv.removeAllViews();
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_id), String.valueOf(rezervniDel.getId())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_skladisce), String.valueOf(rezervniDel.getSkladišče())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_regal), String.valueOf(rezervniDel.getRegal())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_artikel), String.valueOf(rezervniDel.getArtikel())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_znaki), String.valueOf(rezervniDel.getZnaki())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_artikel_dolgi_text), String.valueOf(rezervniDel.getArtikel_dolgi_text())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_proizvajalec), String.valueOf(rezervniDel.getProizvajalec())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_dobavitelj), String.valueOf(rezervniDel.getDobavitelj())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_znesek), String.valueOf(rezervniDel.getZnesek())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_minimalna_zaloga), String.valueOf(rezervniDel.getMinimalna_zaloga())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_dobava), String.valueOf(rezervniDel.getDobava())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_poraba), String.valueOf(rezervniDel.getPoraba())));
        binding.allDataSv.addView(ViewUtils.createTextViewLinearLayoutStartEnd(context, getString(R.string.field_inventura), String.valueOf(rezervniDel.getInventura())));

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
}