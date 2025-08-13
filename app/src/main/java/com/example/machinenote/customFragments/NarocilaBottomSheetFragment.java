package com.example.machinenote.customFragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.machinenote.ApiManager;
import com.example.machinenote.R;
import com.example.machinenote.Utility.CustomDatePicker;
import com.example.machinenote.databinding.FragmentNarocilaBottomSheetBinding;
import com.example.machinenote.models.Narocila;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NarocilaBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentNarocilaBottomSheetBinding binding;
    private Context context;
    private Narocila narocilo;
    private ApiManager apiManager;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat;
    private CustomDatePicker customDatePicker;

    public interface OnDeliveryConfirmedListener {
        void onDeliveryConfirmed();
    }

    private OnDeliveryConfirmedListener listener;

    public static NarocilaBottomSheetFragment newInstance(Context context, Narocila narocilo) {
        NarocilaBottomSheetFragment fragment = new NarocilaBottomSheetFragment();
        fragment.context = context;
        fragment.narocilo = narocilo;
        fragment.apiManager = new ApiManager(context);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        selectedDateTime = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNarocilaBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupDatePicker();
        setupClickListeners();
    }

    private void setupViews() {
        if (narocilo != null) {
            binding.narociloNazivTv.setText(narocilo.getNaziv());
            binding.narociloNarocnikTv.setText(narocilo.getNarocnik());
            binding.narociloLokacijaTv.setText(narocilo.getLokacija());

            // Check if already delivered
            boolean isDelivered = "dostavljeno".equals(narocilo.getStatus()) || "preklicano".equals(narocilo.getStatus());

            if (isDelivered) {
                // Show delivery date if available
                if (narocilo.getDatumPotrjeneDobave() != null && !narocilo.getDatumPotrjeneDobave().isEmpty()) {
                    try {
                        SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        java.util.Date deliveryDate = sqlFormat.parse(narocilo.getDatumPotrjeneDobave());
                        binding.DatumBtn.setText(dateFormat.format(deliveryDate));
                    } catch (Exception e) {
                        binding.DatumBtn.setText(narocilo.getDatumPotrjeneDobave().substring(0, 10));
                    }
                } else {
                    binding.DatumBtn.setText(dateFormat.format(selectedDateTime.getTime()));
                }

                // Disable interactions for delivered orders
                binding.DatumBtn.setEnabled(false);
                binding.potrdiDostavo.setEnabled(false);
                binding.potrdiDostavo.setText("Naročilo zaključeno");
                binding.potrdiDostavo.setAlpha(0.5f);
                binding.DatumBtn.setAlpha(0.5f);
            } else {
                // Set current date as default for non-delivered orders
                binding.DatumBtn.setText(dateFormat.format(selectedDateTime.getTime()));
            }
        } else {
            // Set current date as default
            binding.DatumBtn.setText(dateFormat.format(selectedDateTime.getTime()));
        }
    }

    private void setupDatePicker() {
        // Only setup date picker if not delivered
        if (narocilo != null && ("dostavljeno".equals(narocilo.getStatus()) || "preklicano".equals(narocilo.getStatus()))) {
            return; // Don't setup date picker for delivered orders
        }

        // Create custom date picker with proper listener
        customDatePicker = new CustomDatePicker(getContext(), new CustomDatePicker.ICustomDateListener() {
            @Override
            public void onSet(android.app.Dialog dialog, Calendar calendarSelected,
                              java.util.Date dateSelected, int year, String monthFullName,
                              String monthShortName, int monthNumber, int day,
                              String weekDayFullName, String weekDayShortName) {

                // Update selected date time
                selectedDateTime = calendarSelected;

                // Update UI with selected date
                binding.DatumBtn.setText(dateFormat.format(dateSelected));
            }

            @Override
            public void onCancel() {
                // Handle cancel if needed
            }
        });

        // Set initial date to current date
        customDatePicker.setDate(selectedDateTime);
    }

    private void setupClickListeners() {
        // Date picker - only if not delivered
        binding.DatumBtn.setOnClickListener(v -> {
            if (customDatePicker != null && narocilo != null && (!"dostavljeno".equals(narocilo.getStatus())|| ! "preklicano".equals(narocilo.getStatus()))) {
                customDatePicker.showDialog();
            }
        });

        // Confirm delivery button - only if not delivered
        binding.potrdiDostavo.setOnClickListener(v -> {
            if (narocilo != null && (!"dostavljeno".equals(narocilo.getStatus()) ||!"preklicano".equals(narocilo.getStatus()))){
                confirmDelivery();
            }
        });

        // Cancel button
        binding.prekliclBtn.setOnClickListener(v -> dismiss());
    }

    private void confirmDelivery() {
        if (narocilo == null) {
            Toast.makeText(context, R.string.delivery_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        binding.potrdiDostavo.setEnabled(false);
        binding.potrdiDostavo.setText(R.string.delivery_confirming);

        // Format datum za bazo - set to end of selected day
        Calendar deliveryCalendar = (Calendar) selectedDateTime.clone();
        deliveryCalendar.set(Calendar.HOUR_OF_DAY, 23);
        deliveryCalendar.set(Calendar.MINUTE, 59);
        deliveryCalendar.set(Calendar.SECOND, 59);

        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String deliveryDateTime = sqlDateFormat.format(deliveryCalendar.getTime());

        // Posodobi samo tisto kar rabiš
        narocilo.setStatus("dostavljeno");
        narocilo.setDatumPotrjeneDobave(deliveryDateTime);

        // API klic z že obstoječo metodo
        apiManager.updateNarocilaWithImages(narocilo.getId(), narocilo, null,
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (response.isSuccessful()) {
                                    Toast.makeText(context, R.string.delivery_confirmed, Toast.LENGTH_SHORT).show();
                                    if (listener != null) {
                                        listener.onDeliveryConfirmed();
                                    }
                                    dismiss();
                                } else {
                                    binding.potrdiDostavo.setEnabled(true);
                                    binding.potrdiDostavo.setText(R.string.potrdi_dostavo);
                                    Toast.makeText(context, R.string.delivery_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                binding.potrdiDostavo.setEnabled(true);
                                binding.potrdiDostavo.setText(R.string.potrdi_dostavo);
                                Toast.makeText(context, getString(R.string.delivery_error) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("DeliveryConfirmation", "Error: " + t.getMessage());
                            });
                        }
                    }
                });
    }

    public void setOnDeliveryConfirmedListener(OnDeliveryConfirmedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (customDatePicker != null) {
            customDatePicker.dismissDialog();
        }
    }
}