package com.example.machinenote.Utility;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

                        // Trigger validation if needed
                        if (adapter != null) {
                            // Add any validation logic here if needed
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
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

        dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.setOnDismissListener(dialog -> resetData());

        binding = DialogDatePickerBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        // Set button
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

        // Cancel button
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
            if (calendar_date == null)
                calendar_date = Calendar.getInstance();

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
        if (calendar != null)
            calendar_date = calendar;
    }

    public void setDate(Date date) {
        if (date != null) {
            calendar_date = Calendar.getInstance();
            calendar_date.setTime(date);
        }
    }

    public void setDate(int year, int month, int day) {
        if (month < 12 && month >= 0 && day < 32 && day >= 0 && year > 100
                && year < 3000) {
            calendar_date = Calendar.getInstance();
            calendar_date.set(year, month, day);
        }
    }

    @Override
    public void onClick(View view) {

    }

    public interface ICustomDateListener {
        void onSet(Dialog dialog, Calendar calendarSelected,
                   Date dateSelected, int year, String monthFullName,
                   String monthShortName, int monthNumber, int day,
                   String weekDayFullName, String weekDayShortName);

        void onCancel();
    }

    public static String convertDate(String date, String fromFormat,
                                     String toFormat) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromFormat);
            Date d = simpleDateFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(d);

            simpleDateFormat = new SimpleDateFormat(toFormat);
            simpleDateFormat.setCalendar(calendar);
            date = simpleDateFormat.format(calendar.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }

    private String getMonthFullName(int monthNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthNumber);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM");
        simpleDateFormat.setCalendar(calendar);
        String monthName = simpleDateFormat.format(calendar.getTime());

        return monthName;
    }

    private String getMonthShortName(int monthNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthNumber);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");
        simpleDateFormat.setCalendar(calendar);
        String monthName = simpleDateFormat.format(calendar.getTime());

        return monthName;
    }

    private String getWeekDayFullName(int weekDayNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        simpleDateFormat.setCalendar(calendar);
        String weekName = simpleDateFormat.format(calendar.getTime());

        return weekName;
    }

    private String getWeekDayShortName(int weekDayNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, weekDayNumber);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE");
        simpleDateFormat.setCalendar(calendar);
        String weekName = simpleDateFormat.format(calendar.getTime());

        return weekName;
    }

    private void resetData() {
        calendar_date = null;
    }

    public static String pad(int integerToPad) {
        if (integerToPad >= 10 || integerToPad < 0)
            return String.valueOf(integerToPad);
        else
            return "0" + integerToPad;
    }
}