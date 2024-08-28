package com.example.machinenote.Utility;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.machinenote.R;
import com.example.machinenote.databinding.DialogDateTimePickerBinding;
import com.leondzn.simpleanalogclock.SimpleAnalogClock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomDateTimePicker implements View.OnClickListener {
    private final DialogDateTimePickerBinding binding;

    private Calendar calendar_date = null;

    private final Context context;
    private ICustomDateTimeListener iCustomDateTimeListener = null;

    private final Dialog dialog;

    private boolean is24HourView = true, isAutoDismiss = true;

    private int selectedHour, selectedMinute;

    public static CustomDateTimePicker newInstance(Context context, TextView textTime, SimpleAnalogClock clock) {
        CustomDateTimePicker custom = new CustomDateTimePicker(context,
                new CustomDateTimePicker.ICustomDateTimeListener() {

                    @Override
                    public void onSet(Dialog dialog, Calendar calendarSelected,
                                      Date dateSelected, int year, String monthFullName,
                                      String monthShortName, int monthNumber, int day,
                                      String weekDayFullName, String weekDayShortName,
                                      int hour24, int hour12, int min, int sec,
                                      String AM_PM) {
                        Date date = calendarSelected.getTime();
                        SimpleDateFormat timeFormat = new SimpleDateFormat(TimeDifferenceCalculator.pattern, Locale.getDefault());
                        String time = timeFormat.format(date);
                        textTime.setText(time);
                        clock.setTime(date.getHours(), date.getMinutes(), date.getSeconds());
                    }

                    @Override
                    public void onCancel() {

                    }
                });

        custom.set24HourFormat(true);
        custom.setDate(Calendar.getInstance());
        clock.setOnClickListener(v -> custom.showDialog());
        textTime.setOnClickListener(v -> custom.showDialog());
        return custom;
    }

    public CustomDateTimePicker(Context a, ICustomDateTimeListener customDateTimeListener) {
        context = a;
        iCustomDateTimeListener = customDateTimeListener;

        dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setOnDismissListener(dialog -> resetData());

        binding = DialogDateTimePickerBinding.inflate(dialog.getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        // Set date button
        binding.btnSetDate.setOnClickListener(v -> {


            if (binding.viewSwitcher.getCurrentView() != binding.datePicker) {
                binding.viewSwitcher.showPrevious();
            }
        });

        // Set time button
        binding.btnSetTime.setOnClickListener(v -> {

            if (binding.viewSwitcher.getCurrentView() == binding.datePicker) {
                binding.viewSwitcher.showNext();
            }
        });

        // Set button
        binding.btnSet.setOnClickListener(v -> {
            if (iCustomDateTimeListener != null) {
                int month = binding.datePicker.getMonth();
                int year = binding.datePicker.getYear();
                int day = binding.datePicker.getDayOfMonth();

                calendar_date.set(year, month, day, selectedHour, selectedMinute);

                iCustomDateTimeListener.onSet(dialog, calendar_date,
                        calendar_date.getTime(),
                        calendar_date.get(Calendar.YEAR),
                        getMonthFullName(calendar_date.get(Calendar.MONTH)),
                        getMonthShortName(calendar_date.get(Calendar.MONTH)),
                        calendar_date.get(Calendar.MONTH),
                        calendar_date.get(Calendar.DAY_OF_MONTH),
                        getWeekDayFullName(calendar_date.get(Calendar.DAY_OF_WEEK)),
                        getWeekDayShortName(calendar_date.get(Calendar.DAY_OF_WEEK)),
                        calendar_date.get(Calendar.HOUR_OF_DAY),
                        getHourIn12Format(calendar_date.get(Calendar.HOUR_OF_DAY)),
                        calendar_date.get(Calendar.MINUTE),
                        calendar_date.get(Calendar.SECOND),
                        getAMPM(calendar_date));
            }
            if (dialog.isShowing() && isAutoDismiss) {
                dialog.dismiss();
            }
        });

        // Cancel button
        binding.btnCancel.setOnClickListener(v -> {
            if (iCustomDateTimeListener != null) {
                iCustomDateTimeListener.onCancel();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });

        binding.timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
        });
    }


    public void showDialog() {
        if (!dialog.isShowing()) {
            if (calendar_date == null)
                calendar_date = Calendar.getInstance();

            selectedHour = calendar_date.get(Calendar.HOUR_OF_DAY);
            selectedMinute = calendar_date.get(Calendar.MINUTE);

            binding.timePicker.setIs24HourView(is24HourView);
            binding.timePicker.setCurrentHour(selectedHour);
            binding.timePicker.setCurrentMinute(selectedMinute);

            binding.datePicker.updateDate(calendar_date.get(Calendar.YEAR),
                    calendar_date.get(Calendar.MONTH),
                    calendar_date.get(Calendar.DAY_OF_MONTH));

            dialog.show();

            binding.btnSetTime.performClick();
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

    public void setTimeIn24HourFormat(int hourIn24Format, int minute) {
        if (hourIn24Format < 24 && hourIn24Format >= 0 && minute >= 0
                && minute < 60) {
            if (calendar_date == null)
                calendar_date = Calendar.getInstance();

            calendar_date.set(calendar_date.get(Calendar.YEAR),
                    calendar_date.get(Calendar.MONTH),
                    calendar_date.get(Calendar.DAY_OF_MONTH), hourIn24Format,
                    minute);

            is24HourView = true;
        }
    }

    public void setTimeIn12HourFormat(int hourIn12Format, int minute,
                                      boolean isAM) {
        if (hourIn12Format < 13 && hourIn12Format > 0 && minute >= 0
                && minute < 60) {
            if (hourIn12Format == 12)
                hourIn12Format = 0;

            int hourIn24Format = hourIn12Format;

            if (!isAM)
                hourIn24Format += 12;

            if (calendar_date == null)
                calendar_date = Calendar.getInstance();

            calendar_date.set(calendar_date.get(Calendar.YEAR),
                    calendar_date.get(Calendar.MONTH),
                    calendar_date.get(Calendar.DAY_OF_MONTH), hourIn24Format,
                    minute);

            is24HourView = false;
        }
    }

    public void set24HourFormat(boolean is24HourFormat) {
        is24HourView = is24HourFormat;
    }

    @Override
    public void onClick(View view) {

    }

    public interface ICustomDateTimeListener {
        void onSet(Dialog dialog, Calendar calendarSelected,
                   Date dateSelected, int year, String monthFullName,
                   String monthShortName, int monthNumber, int day,
                   String weekDayFullName, String weekDayShortName, int hour24,
                   int hour12, int min, int sec, String AM_PM);

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

    private int getHourIn12Format(int hour24) {
        int hourIn12Format = 0;

        if (hour24 == 0)
            hourIn12Format = 12;
        else if (hour24 <= 12)
            hourIn12Format = hour24;
        else
            hourIn12Format = hour24 - 12;

        return hourIn12Format;
    }

    private String getAMPM(Calendar calendar) {
        String ampm = (calendar.get(Calendar.AM_PM) == (Calendar.AM)) ? "AM"
                : "PM";
        return ampm;
    }

    private void resetData() {
        calendar_date = null;
        is24HourView = true;
    }

    public static String pad(int integerToPad) {
        if (integerToPad >= 10 || integerToPad < 0)
            return String.valueOf(integerToPad);
        else
            return "0" + integerToPad;
    }
}
