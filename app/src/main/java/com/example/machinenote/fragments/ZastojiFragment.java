package com.example.machinenote.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.DialogFragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.ImageCaptureHelper;
import com.example.machinenote.R;
import com.example.machinenote.Utility.DataPickerDialog;
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
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZastojiFragment extends BaseFragment {

    public String TAG = "Zastoji";
    private List<Linija> linije;
    private Linija linija;
    private List<Sifrant> sifranti;
    private Zastoj zastoj;
    private FragmentZastojiBinding binding;
    private Context context;
    private String idOfLine, sifrant;
    private List<ListViewItem> data;
    private ListViewAdapter adapter;
    ApiManager apiManager;
    private ImageCaptureHelper imageCaptureHelper;

    public ZastojiFragment() {
        // Required empty public constructor
    }

    public static ZastojiFragment newInstance(Context context) {
        ZastojiFragment fragment = new ZastojiFragment();
        fragment.context = context;
        return fragment;
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "onActivityResult called with resultCode: " + result.getResultCode());
                imageCaptureHelper.handleActivityResult(result.getResultCode());
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
        binding = FragmentZastojiBinding.inflate(getLayoutInflater());

        apiManager = new ApiManager(context);

        String[] dataNames = {"Šifrant", "Ime delavca", "Razlog za zaustavitev stroja", "Opomba", "Linija", "Začetni čas", "Končni čas"};

        data = new ArrayList<>();
        for (int i = 0; i < dataNames.length; i++) {
            data.add(new ListViewItem(dataNames[i], false, i + 1));
        }

        // Initialize the adapter
        adapter = new ListViewAdapter(requireContext(), R.layout.custom_listview_item, data);
        binding.listViewStoppages.setAdapter(adapter);

        // Set up TextWatchers
        TextWatcherUtil.addTextWatcherToEditText(binding.imeDelavca, 2, adapter); //textWatcher
        TextWatcherUtil.addTextWatcherToEditText(binding.razlogZaustavitveStroja, 3, adapter);
        TextWatcherUtil.addTextWatcherToEditText(binding.opomba, 4, adapter);


        binding.idOfLineBtn.setOnClickListener(view -> {
            DataPickerDialog.showDialog(view, "Izberi Linijo!",
                    linije.stream().map(Linija::getLinijeSapAndNames).toArray(String[]::new),
                    requireContext(), binding.idOfLineBtn, adapter, 5, selectedIndex -> {
                        if (selectedIndex != -1) {
                            linija = linije.get(selectedIndex);
                            idOfLine = linije.get(selectedIndex).getLinija_SAP();
                        } else {
                            Log.d("mhm2", "Selection was canceled or invalid");
                        }
                    });
        });


        binding.sifrantBtn.setOnClickListener(view -> {
            DataPickerDialog.showDialog(view, "Izberi Šifrant",
                    sifranti.stream().map(Sifrant::getNaziv).toArray(String[]::new),
                    requireContext(), binding.sifrantBtn, adapter, 1, selectedIndex -> {
                        if (selectedIndex != -1) {
                            sifrant = sifranti.get(selectedIndex).getNaziv();
                        } else {
                            Log.d("mhm2", "Selection was canceled or invalid");
                        }
                    });
        });


        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        handleHeightOfStoppages();

        apiCalls();

        binding.sendBtn.setOnClickListener(v -> {
            try {
                sendZastoj();
            } catch (ParseException e) {
                Log.e("error", e.getMessage());
                ((MainActivity) context).showLoadingBar(false);
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
                handleImage(bitmap);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error capturing image: " + error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });


        binding.cameraBtn.setOnClickListener(v -> imageCaptureHelper.captureImage());
        binding.ImagesCameraBtn.setOnClickListener(v -> imageCaptureHelper.captureImage());

        setupTimePickers();

        TextWatcherUtil.addTextWatcherToTextView(binding.textBeforeTime, 6, adapter);
        TextWatcherUtil.addTextWatcherToTextView(binding.textAfterTime, 7, adapter);
        adapter.updateItemStatus(6, false);
        adapter.updateItemStatus(7, false);

        binding.dezurstvoTv.setOnClickListener(v -> binding.dezurstvoCheckBox.setChecked(binding.dezurstvoCheckBox.isChecked() ? false : true));

        return binding.getRoot();
    }

    private void setupTimePickers() {
        binding.analogClockBefore.setOnClickListener(v -> showDateTimePicker(true));

        binding.textBeforeTime.setOnClickListener(v -> showDateTimePicker(true));

        binding.analogClockAfter.setOnClickListener(v -> showDateTimePicker(false));
        binding.textAfterTime.setOnClickListener(v -> showDateTimePicker(false));
    }

    private void showDateTimePicker(boolean isBeforeTime) {
        // Create a new instance of SwitchDateTimeDialogFragment
        SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                isBeforeTime ? "Select Before Time" : "Select After Time",
                "OK",
                "Cancel"
        );

        // Set 24-hour mode
        dateTimeDialogFragment.set24HoursMode(true);

        // Optionally set default date/time
        dateTimeDialogFragment.setDefaultDateTime(new GregorianCalendar().getTime());

        // Define the format for date and time
        try {
            dateTimeDialogFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        // Set up button click listeners
        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                SimpleDateFormat timeFormat = new SimpleDateFormat(TimeDifferenceCalculator.pattern, Locale.getDefault());
                String time = timeFormat.format(date);

                if (isBeforeTime) {
                    binding.textBeforeTime.setText(time);
                    binding.analogClockBefore.setTime(date.getHours(), date.getMinutes(), date.getSeconds());
                    // Update AnalogClock or other UI elements here if needed
                } else {
                    binding.textAfterTime.setText(time);
                    binding.analogClockAfter.setTime(date.getHours(), date.getMinutes(), date.getSeconds());
                    // Update AnalogClock or other UI elements here if needed
                }
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Handle negative button click if needed
            }
        });

        // Show the dialog
        dateTimeDialogFragment.show(((MainActivity) context).getSupportFragmentManager(), "dialog_time");
    }


    private void handleImage(Bitmap bitmap) {
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        float aspectRatio = (float) imageHeight / imageWidth;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        int maxWidth = getResources().getDisplayMetrics().widthPixels; // Width of the screen or parent
        params.height = (int) (maxWidth * aspectRatio); // Height adjusted by aspect ratio
        imageView.setLayoutParams(params);
        imageView.setPadding(0, 0, 0, 0);

        imageView.setOnClickListener(v -> showDeleteConfirmationDialog(imageView, bitmap));

        binding.imagesLlInSv.addView(imageView);

    }

    private void showDeleteConfirmationDialog(ImageView imageView, Bitmap bitmap) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Remove the image from the layout
                    binding.imagesLlInSv.removeView(imageView);
                    // Optionally, you can also recycle the bitmap if it's no longer needed
                    bitmap.recycle();
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void handleHeightOfStoppages() {
        binding.stoppagesLl.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Remove the listener to avoid multiple calls
                        binding.stoppagesLl.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int height = binding.stoppagesLl.getHeight();

                        if (height < binding.stoppagesLl.getMinimumHeight()) {
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (int) (80 * getResources().getDisplayMetrics().density) // Convert 64dp to pixels
                            );
                            int marginBottom = (int) (8 * getResources().getDisplayMetrics().density); // Convert 16dp to pixels
                            layoutParams.setMargins(0, 0, 0, marginBottom);
                            binding.stoppagesLl.setLayoutParams(layoutParams);
                        }
                    }
                }
        );
    }

    private List<File> getImagesFromLayout() {
        List<File> imageFiles = new ArrayList<>();
        int childCount = binding.imagesLlInSv.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View view = binding.imagesLlInSv.getChildAt(i);
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                File file = bitmapToFile(bitmap, "image_" + i + ".jpg");
                imageFiles.add(file);
            }
        }

        return imageFiles;
    }

    private File bitmapToFile(Bitmap bitmap, String fileName) {
        File file = new File(requireContext().getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos); //todo maybe put settings for this
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
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
                "qr koda");
    }

    private String getDezurstvo() {
        if (binding.dezurstvoCheckBox.isChecked()) {
            return "DA";
        } else {
            return "NE";
        }
    }

    private void sendZastoj() throws ParseException {
        if (adapter.areAllItemsComplete()) {
            List<File> imageFiles = getImagesFromLayout();

            zastoj = createZastoj();
            ((MainActivity) context).showLoadingBar(true);
            // Call sendZastojWithImages
            apiManager.sendZastojWithImages(zastoj, imageFiles, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Poslano!" , Toast.LENGTH_SHORT).show();
                        ((MainActivity) context).clearLastFragmentFromBackStack();
                        ((MainActivity) context).showLoadingBar(false);
                    } else {
                        Toast.makeText(context, "error: " + response.message(), Toast.LENGTH_SHORT).show();
                        ((MainActivity) context).showLoadingBar(false);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).showLoadingBar(false);
                }
            });
        } else {
            Toast.makeText(context, "Izpolni vsa polja", Toast.LENGTH_SHORT).show();
            ((MainActivity) context).showLoadingBar(false);
        }
    }

    private void apiCalls() {
        {
            apiManager.fetchSifrants(new ApiManager.SifrantCallback() {
                @Override
                public void onSuccess(List<Sifrant> sifrants) {
                    sifranti = sifrants;
                }

                @Override
                public void onFailure(String errorMessage) {

                }
            });
        }

        {
            // Fetch zastoji data
            apiManager.getZastoji(new ApiManager.ZastojiCallback() {
                @Override
                public void onSuccess(List<Zastoj> zastoji) {

                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(context, "Failed to fetch zastoji: " + errorMessage, Toast.LENGTH_SHORT).show();
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
                    System.err.println("Error: " + errorMessage);
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
}
