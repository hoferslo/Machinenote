package com.example.machinenote.Utility;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.example.machinenote.R;
import com.example.machinenote.databinding.DialogDatePickerBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomDatePicker implements View.OnClickListener {
    private final DialogDatePickerBinding binding;
    private Calendar calendar_date = null;
    private final Context context;
    private ICustomDateListener iCustomDateListener = null;
    private final Dialog dialog;
    private boolean isAutoDismiss = true;

    public static CustomDatePicker newInstance(Context context, Button textDate, ListViewAdapter adapter) {
        CustomDatePicker custom = new CustomDatePicker(context,
                new CustomDatePicker.ICustomDateListener() {
                    @Override
                    public void onSet(Dialog dialog, Calendar calendarSelected,
                                      Date dateSelected, int year, String monthFullName,
                                      String monthShortName, int monthNumber, int day,
                                      String weekDayFullName, String weekDayShortName) {
                        Date date = calendarSelected.getTime();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String dateString = dateFormat.format(date);
                        textDate.setText(dateString);

                        if (adapter != null) {
                            // Tukaj dodaš poljubno validacijo, če je potrebna
                        }
                    }

                    @Override
                    public void onCancel() {}
                });

        custom.setDate(Calendar.getInstance());
        textDate.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            custom.showDialog();
        });
        return custom;
    }

    public CustomDatePicker(Context a, ICustomDateListener customDateListener) {
        context = a;
        iCustomDateListener = customDateListener;

        // Inicializacija koledarja na trenutni čas, da nikoli ni null
        calendar_date = Calendar.getInstance();

        dialog = new Dialog(context, R.style.CustomDialogTheme);
        // POPRAVEK: Odstranjen resetData(), da ne brišemo reference koledarja

        binding = DialogDatePickerBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        // Gumb Potrdi
        binding.btnSet.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            if (iCustomDateListener != null) {
                int month = binding.datePicker.getMonth();
                int year = binding.datePicker.getYear();
                int day = binding.datePicker.getDayOfMonth();

                calendar_date.set(year, month, day);

                iCustomDateListener.onSet(dialog, calendar_date,
                        calendar_date.getTime(),
                        calendar_date.get(Calendar.YEAR),
                        getMonthFullName(calendar_date.get(Calendar.MONTH)),
                        getMonthShortName(calendar_date.get(Calendar.MONTH)),
                        calendar_date.get(Calendar.MONTH),
                        calendar_date.get(Calendar.DAY_OF_MONTH),
                        getWeekDayFullName(calendar_date.get(Calendar.DAY_OF_WEEK)),
                        getWeekDayShortName(calendar_date.get(Calendar.DAY_OF_WEEK)));
            }
            if (dialog.isShowing() && isAutoDismiss) {
                dialog.dismiss();
            }
        });

        // Gumb Prekliči
        binding.btnCancel.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            if (iCustomDateListener != null) {
                iCustomDateListener.onCancel();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });
    }

    public void showDialog() {
        if (!dialog.isShowing()) {
            if (calendar_date == null) {
                calendar_date = Calendar.getInstance();
            }

            // Osvežimo vizualni DatePicker na vrednosti iz koledarja pred prikazom
            binding.datePicker.updateDate(calendar_date.get(Calendar.YEAR),
                    calendar_date.get(Calendar.MONTH),
                    calendar_date.get(Calendar.DAY_OF_MONTH));

            dialog.show();
        }
    }

    public void setAutoDismiss(boolean isAutoDismiss) {
        this.isAutoDismiss = isAutoDismiss;
    }

    public void dismissDialog() {
        if (dialog.isShowing())
            dialog.dismiss();
    }

    public void setDate(Calendar calendar) {
        if (calendar != null) {
            // Kloniramo ali kopiramo vrednosti, da preprečimo neželene stranske učinke referenc
            if (calendar_date == null) calendar_date = Calendar.getInstance();
            calendar_date.setTimeInMillis(calendar.getTimeInMillis());
        }
    }

    public void setDate(Date date) {
        if (date != null) {
            if (calendar_date == null) calendar_date = Calendar.getInstance();
            calendar_date.setTime(date);
        }
    }

    public void setDate(int year, int month, int day) {
        if (month < 12 && month >= 0 && day < 32 && day >= 0 && year > 100 && year < 3000) {
            if (calendar_date == null) calendar_date = Calendar.getInstance();
            calendar_date.set(year, month, day);
        }
    }

    @Override
    public void onClick(View view) {}

    public interface ICustomDateListener {
        void onSet(Dialog dialog, Calendar calendarSelected,
                   Date dateSelected, int year, String monthFullName,
                   String monthShortName, int monthNumber, int day,
                   String weekDayFullName, String weekDayShortName);
        void onCancel();
    }

    // Pomožne metode za formate imen mesecev in dni ostanejo nespremenjene...
    private String getMonthFullName(int monthNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthNumber);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }

    private String getMonthShortName(int monthNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthNumber);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }

    private String getWeekDayFullName(int weekDayNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }

    private String getWeekDayShortName(int weekDayNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }
}