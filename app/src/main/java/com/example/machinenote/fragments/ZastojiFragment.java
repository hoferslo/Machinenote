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
import com.example.machinenote.Utility.TimeDifferenceCalculator;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentZastojiBinding;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.ListViewItem;
import com.example.machinenote.models.Sifrant;
import com.example.machinenote.models.Zastoj;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class    ZastojiFragment extends BaseFragment implements QRCodeScannerFragment.QRCodeScanCallback {

    private List<Linija> linije;
    private Linija linija;
    private List<Sifrant> sifranti;
    private Zastoj zastoj;
    private FragmentZastojiBinding binding;
    private Context context;
    private List<ListViewItem> data;
    private ListViewAdapter adapter;
    ApiManager apiManager;
    private ImageCaptureHelper imageCaptureHelper;
    private String qrKoda = "NE";
    SharedPreferencesHelper sharedPreferencesHelper;

    public ZastojiFragment() {
        // Required empty public constructor
    }

    public static ZastojiFragment newInstance(Context context) {
        ZastojiFragment fragment = new ZastojiFragment();
        fragment.context = context;
        fragment.TAG = context.getString(R.string.tag_zastoji);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "onActivityResult called with resultCode: " + result.getResultCode());
                imageCaptureHelper.handleActivityResult(result.getResultCode(), result.getData());
            }
    );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());
        binding = FragmentZastojiBinding.inflate(getLayoutInflater());

        apiManager = new ApiManager(context);

        String[] dataNames = {getString(R.string.sifrant), getString(R.string.name_of_worker), getString(R.string.reason_for_stoppage), getString(R.string.opomba), getString(R.string.line), getString(R.string.start_time), getString(R.string.end_time)};

        data = new ArrayList<>();
        for (int i = 0; i < dataNames.length; i++) {
            data.add(new ListViewItem(dataNames[i], false, i + 1));
        }
        data.add(new ListViewItem(getString(R.string.time_negative_error), false, 999));
        // Initialize the adapter
        adapter = new ListViewAdapter(requireContext(), R.layout.custom_listview_item, data);
        binding.requiredItemsLv.setAdapter(adapter);

        // Set up TextWatchers
        String maintainerName = sharedPreferencesHelper.getUsername();
        if (maintainerName != null && !maintainerName.isEmpty()) {
            binding.imeDelavca.setText(maintainerName);
        }

        TextWatcherUtil.addTextWatcherToEditText(binding.imeDelavca, 2, adapter); //textWatcher
        TextWatcherUtil.addTextWatcherToEditText(binding.razlogZaustavitveStroja, 3, adapter);
        TextWatcherUtil.addTextWatcherToEditText(binding.opomba, 4, adapter);


        binding.idOfLineBtn.setOnClickListener(view -> {
            DataPickerDialog.showDialog(view, getString(R.string.pick_line),
                    linije.stream().map(Linija::getLinijeSapAndNames).toArray(String[]::new),
                    requireContext(), binding.idOfLineBtn, adapter, 5, selectedIndex -> {
                        if (selectedIndex != -1) {
                            setLinija(linije.get(selectedIndex));
                            qrKoda = getString(R.string.QR_koda_value_negative);
                            TextWatcherUtil.handleHeightOfStoppages(context, binding.requiredItemsLl);
                        }
                    });
        });


        binding.sifrantBtn.setOnClickListener(view -> {
            DataPickerDialog.showDialog(view, getString(R.string.pick_sifrant),
                    sifranti.stream().map(Sifrant::getNaziv).toArray(String[]::new),
                    requireContext(), binding.sifrantBtn, adapter, 1, selectedIndex -> {
                        TextWatcherUtil.handleHeightOfStoppages(context, binding.requiredItemsLl);
                    });
        });

        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        apiCalls();

        binding.sendBtn.setOnClickListener(v -> {
            try {
                /*
                if(TimeDifferenceCalculator.getMinutesBetweenDates(binding.textBeforeTime.getText().toString(), binding.textAfterTime.getText().toString()) < 0){
                    Toast.makeText(context, "Čas je narobe nastavljen", Toast.LENGTH_SHORT).show();
                    return;
                }
                */
                sendZastoj();
            } catch (ParseException e) {
                Log.e("error", e.getMessage());
                ((MainActivity) context).showLoadingBar(false, "");
            }
        });

        binding.tabEntryBtn.setOnClickListener(v -> handleTabPress(1));
        binding.tabPicturesBtn.setOnClickListener(v -> handleTabPress(2));

        imageCaptureHelper = new ImageCaptureHelper(requireContext(), cameraLauncher);
        imageCaptureHelper.setImageCaptureCallback(new ImageCaptureHelper.ImageCaptureCallback() {
            @Override
            public void onImageCaptured(Bitmap bitmap) {
                Log.d(TAG, "Image captured successfully.");
                ImageHelper.handleImage(context, bitmap, binding.imagesContainer);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error capturing image: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });


        binding.cameraBtn.setOnClickListener(v -> imageCaptureHelper.captureImage());
        binding.ImagesCameraBtn.setOnClickListener(v -> imageCaptureHelper.captureImage());

        CustomDateTimePicker.newInstance(context, binding.textBeforeTime, binding.analogClockBefore, adapter, binding.textBeforeTime, binding.textAfterTime);
        CustomDateTimePicker.newInstance(context, binding.textAfterTime, binding.analogClockAfter, adapter, binding.textBeforeTime, binding.textAfterTime);

        TextWatcherUtil.addTextWatcherToTextViewWithTimeValidation(binding.textBeforeTime, 6, adapter, binding.textBeforeTime, binding.textAfterTime);
        TextWatcherUtil.addTextWatcherToTextViewWithTimeValidation(binding.textAfterTime, 7, adapter, binding.textBeforeTime, binding.textAfterTime);
        adapter.updateItemStatus(6, false);
        adapter.updateItemStatus(7, false);


        binding.dezurstvoTv.setOnClickListener(v -> binding.dezurstvoCheckBox.setChecked(!binding.dezurstvoCheckBox.isChecked()));

        binding.scanBtn.setOnClickListener(v -> startQRCodeScanner());


        TextWatcherUtil.handleHeightOfStoppages(context, binding.requiredItemsLl);

        return binding.getRoot();
    }

    private void startQRCodeScanner() {
        ((MainActivity) context).loadFragment(QRCodeScannerFragment.newInstance(context, this));
    }

    public void setLinija(Linija l) {
        linija = l;
    }

    private Zastoj createZastoj() throws ParseException {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        String user = sharedPreferencesHelper.getString("Username", "invalid user");

        return new Zastoj(
                user,
                String.valueOf(linija.getNaziv_linije()),
                String.valueOf(binding.sifrantBtn.getText()),
                String.valueOf(binding.opomba.getText()),
                String.valueOf(binding.razlogZaustavitveStroja.getText()),
                String.valueOf(binding.textBeforeTime.getText()),
                String.valueOf(binding.textAfterTime.getText()),
                String.valueOf(binding.imeDelavca.getText()),
                String.valueOf(linija.getLinija_SAP()),
                "",
                getDezurstvo(),
                TimeDifferenceCalculator.getMinutesBetweenDates(binding.textBeforeTime.getText().toString(), binding.textAfterTime.getText().toString()),
                TimeDifferenceCalculator.getYearFromDateString(binding.textBeforeTime.getText().toString()), //a sploh hocem to
                TimeDifferenceCalculator.getMonthFromDateString(binding.textBeforeTime.getText().toString()),
                qrKoda);
    }

    private String getDezurstvo() {
        if (binding.dezurstvoCheckBox.isChecked()) {
            return getString(R.string.dezurstvo_value_positive);
        } else {
            return getString(R.string.dezurstvo_value_negative);
        }
    }

    private void sendZastoj() throws ParseException {
        if (adapter.areAllItemsComplete()) {
            List<File> imageFiles = ImageHelper.getImagesFromLayout(context, binding.imagesContainer);

            zastoj = createZastoj();
            ((MainActivity) context).showLoadingBar(true, getString(R.string.sending_stoppage));
            // Call sendZastojWithImages
            apiManager.sendZastojWithImages(zastoj, imageFiles, new Callback<Void>() {
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
            //Toast.makeText(context, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            //            ((MainActivity) context).showLoadingBar(false, "");
            // premaknu v ListViewAdapter zarad vec errorjev
        }
    }

    private void apiCalls() {
        // Initialize lists to prevent null pointer exceptions
        linije = new ArrayList<>();
        sifranti = new ArrayList<>();

        // Disable buttons until data is loaded
        binding.idOfLineBtn.setEnabled(false);
        binding.sifrantBtn.setEnabled(false);

        // Fetch sifranti
        apiManager.fetchSifrants(new ApiManager.SifrantCallback() {
            @Override
            public void onSuccess(List<Sifrant> sifrants) {
                sifranti = sifrants;
                binding.sifrantBtn.setEnabled(true);
                Log.d(TAG, "Sifranti loaded: " + sifrants.size());
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Napaka pri nalaganju sifrantov: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Sifrant fetch failed: " + errorMessage);
            }
        });

        // Fetch linije
        apiManager.fetchLinije(new ApiManager.LinijeCallback() {
            @Override
            public void onSuccess(List<Linija> response) {
                if (response != null && !response.isEmpty()) {
                    linije = response;
                    binding.idOfLineBtn.setEnabled(true);

                    // Optional: Update button text to show data is loaded
                    binding.idOfLineBtn.setText(getString(R.string.pick_line));

                    Log.d(TAG, "Linije loaded: " + response.size());
                    Log.d(TAG, "First linija: " + response.get(0).toString());
                } else {
                    Toast.makeText(context, "Ni razpoložljivih linij", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Linije response is empty or null");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Napaka pri nalaganju linij: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Linije fetch failed: " + errorMessage);

                // Keep button disabled on error
                binding.idOfLineBtn.setEnabled(false);
            }
        });
    }

    public void handleTabPress(int position) {
        binding.entryLl.setVisibility(View.INVISIBLE);
        binding.imagesLl.setVisibility(View.INVISIBLE);
        if (position == 1) {
            binding.entryLl.setVisibility(View.VISIBLE);
        } else if (position == 2) {
            binding.imagesLl.setVisibility(View.VISIBLE);
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
                    binding.idOfLineBtn.setText(linija.getLinijeSapAndNames()); // TODO: dodaj ko je qr scanned da ti nastela uro
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
