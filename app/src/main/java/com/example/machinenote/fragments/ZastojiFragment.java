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

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.ImageCaptureHelper;
import com.example.machinenote.R;
import com.example.machinenote.Utility.DataPickerDialog;
import com.example.machinenote.Utility.ListViewAdapter;
import com.example.machinenote.Utility.TextWatcherUtil;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentZastojiBinding;
import com.example.machinenote.models.Linija;
import com.example.machinenote.models.ListViewItem;
import com.example.machinenote.models.Sifrant;
import com.example.machinenote.models.Zastoj;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZastojiFragment extends BaseFragment {

    public String TAG = "Zastoji";
    private List<Linija> linije;
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

        String[] dataNames = {"Šifrant", "Ime delavca", "Razlog za zaustavitev stroja", "Opomba", "Linija"};

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
            int tmp = DataPickerDialog.showDialog(view, "Izberi Linijo!",
                    linije.stream().map(Linija::getLinija_SAP).toArray(String[]::new),
                    requireContext(), binding.idOfLineBtn, adapter, 5);
            if (tmp != -1) {
                idOfLine = linije.get(tmp).getLinija_SAP();
            }
        });

        binding.sifrantBtn.setOnClickListener(view -> {
            int tmp = DataPickerDialog.showDialog(view, "Izberi Šifrant", sifranti.stream().map(Sifrant::getNaziv).toArray(String[]::new), requireContext(), binding.sifrantBtn, adapter, 1);
            if (tmp != -1) {
                sifrant = sifranti.get(tmp).getNaziv();
            }
        });

        binding.cancelBtn.setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.onBackPressed();
        });

        handleHeightOfStoppages();

        apiCalls();

        binding.sendBtn.setOnClickListener(v -> sendZastoj());

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


        return binding.getRoot();
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
                            int marginBottom = (int) (16 * getResources().getDisplayMetrics().density); // Convert 16dp to pixels
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    private void sendZastoj(){
        if (adapter.areAllItemsComplete()) {
            List<File> imageFiles = getImagesFromLayout();

            // Call sendZastojWithImages
            apiManager.sendZastojWithImages(zastoj, imageFiles, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Poslano!" , Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "error: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Izpolni vsa polja", Toast.LENGTH_SHORT).show();
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
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);

    }
}
