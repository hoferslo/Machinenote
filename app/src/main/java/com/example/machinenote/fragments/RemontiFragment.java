package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.CustomDateTimePicker;
import com.example.machinenote.Utility.DataPickerDialog;
import com.example.machinenote.Utility.HandleQRCode;
import com.example.machinenote.Utility.ImageCaptureHelper;
import com.example.machinenote.Utility.ImageHelper;
import com.example.machinenote.Utility.ListViewAdapter;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.Utility.TextWatcherUtil;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentRemontiBinding;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.ListViewItem;
import com.example.machinenote.models.Remont;
import com.example.machinenote.models.SklopLinije;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RemontiFragment extends BaseFragment implements QRCodeScannerFragment.QRCodeScanCallback {

    FragmentRemontiBinding binding;
    Context context;

    private List<Linija> linije;
    private List<SklopLinije> sklopiLinij = new ArrayList<>();
    private List<SklopLinije> sklopiLinijGledeNaIzbranoLinijo = new ArrayList<>();
    private Linija linija;
    private Remont remont;
    private List<ListViewItem> data;
    private ListViewAdapter adapter;
    ApiManager apiManager;
    private ImageCaptureHelper imageCaptureHelper;
    private String qrKoda = "NE";


    public static RemontiFragment newInstance(Context context) {
        RemontiFragment fragment = new RemontiFragment();
        fragment.context = context;
        fragment.TAG = context.getString(R.string.tag_remonti);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "onActivityResult called with resultCode: " + result.getResultCode());
                imageCaptureHelper.handleActivityResult(result.getResultCode());
            }
    );

    public static RemontiFragment newInstance(String param1, String param2) {
        RemontiFragment fragment = new RemontiFragment();
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
        binding = FragmentRemontiBinding.inflate(getLayoutInflater());

        apiManager = new ApiManager(context);

        String[] dataNames = {getString(R.string.line), getString(R.string.sklop_linije), getString(R.string.zamenjani_deli), getString(R.string.opomba), getString(R.string.start_time), getString(R.string.end_time)};

        data = new ArrayList<>();
        for (int i = 0; i < dataNames.length; i++) {
            data.add(new ListViewItem(dataNames[i], false, i));
        }

        // Initialize the adapter
        adapter = new ListViewAdapter(requireContext(), R.layout.custom_listview_item, data);
        binding.requiredItemsLv.setAdapter(adapter);

        TextWatcherUtil.addTextWatcherToEditText(binding.zamenjaniDeli, 2, adapter);
        TextWatcherUtil.addTextWatcherToEditText(binding.opomba, 3, adapter);


        binding.idOfLineBtn.setOnClickListener(view -> {
            DataPickerDialog.showDialog(view, getString(R.string.pick_line),
                    linije.stream().map(Linija::getLinijeSapAndNames).toArray(String[]::new),
                    requireContext(), binding.idOfLineBtn, adapter, 0, selectedIndex -> {
                        if (selectedIndex != -1) {
                            Log.d("nekejnkeje", "linija index: " + selectedIndex);
                            setLinija(linije.get(selectedIndex));
                            qrKoda = getString(R.string.QR_koda_value_negative);
                            TextWatcherUtil.handleHeightOfStoppages(context, binding.requiredItemsLl);
                        }
                    });
        });


        binding.sklopLinijeBtn.setOnClickListener(view -> {
            if (!sklopiLinijGledeNaIzbranoLinijo.isEmpty()) {
                DataPickerDialog.showDialog(view, getString(R.string.pick_sklop_linije),
                        sklopiLinijGledeNaIzbranoLinijo.stream().map(SklopLinije::getSklopLinije).toArray(String[]::new),
                        requireContext(), binding.sklopLinijeBtn, adapter, 1, selectedIndex -> {
                            TextWatcherUtil.handleHeightOfStoppages(context, binding.requiredItemsLl);
                        });
            } else {
                Toast.makeText(context, R.string.najprej_izberi_linijo, Toast.LENGTH_SHORT).show();
            }
        });


        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        apiCalls();

        binding.sendBtn.setOnClickListener(v -> {
            try {
                sendRemont();
            } catch (ParseException e) {
                Log.e("error", e.getMessage());
                ((MainActivity) context).showLoadingBar(false, "");
            }
        });

        binding.tabEntryBtn.setOnClickListener(v -> handleTabPress(1));
        binding.tabPicturesBtn.setOnClickListener(v -> handleTabPress(2));
        binding.tabInfoBtn.setOnClickListener(v -> handleTabPress(3));

        imageCaptureHelper = new ImageCaptureHelper(requireContext(), cameraLauncher);
        imageCaptureHelper.setImageCaptureCallback(new ImageCaptureHelper.ImageCaptureCallback() {
            @Override
            public void onImageCaptured(Bitmap bitmap) {
                Log.d(TAG, "Image captured successfully.");
                ImageHelper.handleImage(context, bitmap, binding.imagesLlInSv);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error capturing image: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });


        binding.cameraBtn.setOnClickListener(v -> imageCaptureHelper.captureImage());
        binding.ImagesCameraBtn.setOnClickListener(v -> imageCaptureHelper.captureImage());

        CustomDateTimePicker.newInstance(context, binding.textBeforeTime, binding.analogClockBefore);
        CustomDateTimePicker.newInstance(context, binding.textAfterTime, binding.analogClockAfter);

        TextWatcherUtil.addTextWatcherToTextView(binding.textBeforeTime, 4, adapter);
        TextWatcherUtil.addTextWatcherToTextView(binding.textAfterTime, 5, adapter);
        adapter.updateItemStatus(4, false);
        adapter.updateItemStatus(5, false);

        binding.scanBtn.setOnClickListener(v -> startQRCodeScanner());

        TextWatcherUtil.handleHeightOfStoppages(context, binding.requiredItemsLl);

        return binding.getRoot();
    }

    private void startQRCodeScanner() {
        ((MainActivity) context).loadFragment(QRCodeScannerFragment.newInstance(context, this));
    }

    public void setLinija(Linija l) {
        linija = l;
        Log.d("nekejnkeje", "linija: " + linija.getLinija_SAP() + " : " + linija.getNaziv_linije());
        sklopiLinijGledeNaIzbranoLinijo = SklopLinije.getSklopLinijBasedOnLine(linija, sklopiLinij);
    }

    private Remont createRemont() throws ParseException {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        String user = sharedPreferencesHelper.getString("Username", "invalid user");

        return new Remont(0,
                user,
                String.valueOf(linija.getNaziv_linije()),
                String.valueOf(binding.sklopLinijeBtn.getText()),
                String.valueOf(binding.opomba.getText()),
                String.valueOf(binding.zamenjaniDeli.getText()),
                String.valueOf(binding.textBeforeTime.getText()),
                String.valueOf(binding.textAfterTime.getText()),
                String.valueOf(linija.getLinija_SAP()),
                "");
    }

    private void sendRemont() throws ParseException {
        if (adapter.areAllItemsComplete()) {
            List<File> imageFiles = ImageHelper.getImagesFromLayout(context, binding.imagesLlInSv);

            remont = createRemont();
            ((MainActivity) context).showLoadingBar(true, getString(R.string.sending_stoppage));
            // Call sendZastojWithImages
            apiManager.sendRemontWithImages(remont, imageFiles, new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, getString(R.string.sent), Toast.LENGTH_SHORT).show();
                        ((MainActivity) context).clearLastFragmentFromBackStack();
                        ((MainActivity) context).showLoadingBar(false, "");
                    } else {
                        Toast.makeText(context, getString(R.string.error) + response.message(), Toast.LENGTH_SHORT).show();
                        ((MainActivity) context).showLoadingBar(false, "");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, getString(R.string.error) + t.getMessage(), Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).showLoadingBar(false, "");
                }
            });
        } else {
            Toast.makeText(context, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            ((MainActivity) context).showLoadingBar(false, "");
        }
    }

    private void apiCalls() {
        {
            apiManager.fetchSklopeLinij(new ApiManager.SklopLinijeCallback() {
                @Override
                public void onSuccess(List<SklopLinije> response) {
                    sklopiLinij = response;
                }

                @Override
                public void onFailure(String errorMessage) {

                }
            });
        }

        {
            apiManager.fetchLinije(new ApiManager.LinijeCallback() {
                @Override
                public void onSuccess(List<Linija> response) {
                    linije = response;
                }

                @Override
                public void onFailure(String errorMessage) {
                    System.err.println(getString(R.string.error) + errorMessage);
                }
            });
        }
    }

    public void handleTabPress(int position) {
        binding.entryLl.setVisibility(View.INVISIBLE);
        binding.imagesLl.setVisibility(View.INVISIBLE);
        binding.infoLl.setVisibility(View.INVISIBLE);
        if (position == 1) {
            binding.entryLl.setVisibility(View.VISIBLE);
        } else if (position == 2) {
            binding.imagesLl.setVisibility(View.VISIBLE);
        } else if (position == 3) {
            binding.infoLl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) context).binding.toolbarTitle.setText(TAG);
    }

    @Override
    public void onQRCodeScanned(String qrCode) {
        requireActivity().runOnUiThread(() -> {
            boolean success = false;
            String linijaSap = HandleQRCode.getLinijaSapFromQR(qrCode);
            for (Linija l : linije) {
                if (l.getLinija_SAP().equals(linijaSap)) {
                    setLinija(l);
                    binding.idOfLineBtn.setText(linija.getLinijeSapAndNames());
                    adapter.updateItemStatus(5, true);
                    success = true;
                    qrKoda = getString(R.string.QR_koda_value_positive);
                    break;
                }
            }
            if (!success) {
                Toast.makeText(context, getString(R.string.line_not_exist), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onScanCancelled() {
        Toast.makeText(getContext(), getString(R.string.scan_cancelled), Toast.LENGTH_LONG).show();
    }
}