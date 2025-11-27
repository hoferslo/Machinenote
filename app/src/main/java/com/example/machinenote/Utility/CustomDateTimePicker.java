package com.example.machinenote.Utility;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ParseException;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.machinenote.R;
import com.example.machinenote.databinding.DialogDateTimePickerBinding;
import com.leondzn.simpleanalogclock.SimpleAnalogClock;

import java.lang.reflect.Field;
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
    private static boolean is24HourView = true;
    private boolean isAutoDismiss = true;
    private int selectedHour, selectedMinute;

    public static CustomDateTimePicker newInstance(Context context, TextView textTime, SimpleAnalogClock clock, ListViewAdapter adapter, TextView startTimeTextView, TextView endTimeTextView) {
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

                        if (adapter != null && startTimeTextView != null && endTimeTextView != null) {
                            TextWatcherUtil.validateTimeOrderWithCalculator(startTimeTextView, endTimeTextView, adapter);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });

        custom.set24HourFormat(is24HourView);
        custom.setDate(Calendar.getInstance());
        clock.setOnClickListener(v -> custom.showDialog());
        textTime.setOnClickListener(v -> custom.showDialog());
        return custom;
    }

    public static String getCurrentDateTime() {
        // Use TimeDifferenceCalculator.pattern to ensure format consistency
        SimpleDateFormat dateFormat = new SimpleDateFormat(TimeDifferenceCalculator.pattern, Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public static void setTimeDirectly(
            TextView textView,
            SimpleAnalogClock clock,
            String dateTime,
            ListViewAdapter adapter,
            int adapterPosition,
            TextView beforeTimeView,
            TextView afterTimeView
    ) {
        try {
            // Parse the date time string using the pattern from TimeDifferenceCalculator
            SimpleDateFormat dateFormat = new SimpleDateFormat(TimeDifferenceCalculator.pattern, Locale.getDefault());
            Date date = dateFormat.parse(dateTime);

            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // Update the TextView
                textView.setText(dateTime);

                // Update the SimpleAnalogClock
                // Note: setTime expects hours, minutes, seconds
                clock.setTime(
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        calendar.get(Calendar.SECOND)
                );

                // Update adapter status
                adapter.updateItemStatus(adapterPosition, true);

                // Trigger time validation
                TextWatcherUtil.validateTimeOrderWithCalculator(beforeTimeView, afterTimeView, adapter);

                Log.d("CustomDateTimePicker", "Time set successfully: " + dateTime);
            }
        } catch (ParseException | java.text.ParseException e) {
            Log.e("CustomDateTimePicker", "Error parsing date: " + dateTime, e);
            throw new RuntimeException(e);
        }
    }



    public CustomDateTimePicker(Context a, ICustomDateTimeListener customDateTimeListener) {
        context = a;
        iCustomDateTimeListener = customDateTimeListener;

        dialog = new Dialog(context, R.style.CustomDialogTheme);
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
                applyTimePickerFixes();
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

        // Time change listener
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

            // Set current time
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                binding.timePicker.setHour(selectedHour);
                binding.timePicker.setMinute(selectedMinute);
            } else {
                binding.timePicker.setCurrentHour(selectedHour);
                binding.timePicker.setCurrentMinute(selectedMinute);
            }

            binding.datePicker.updateDate(calendar_date.get(Calendar.YEAR),
                    calendar_date.get(Calendar.MONTH),
                    calendar_date.get(Calendar.DAY_OF_MONTH));

            dialog.show();
            binding.btnSetTime.performClick();

            // Apply fixes with delay
            binding.timePicker.post(() -> {
                applyTimePickerFixes();

                // Re-set values to ensure proper display
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    binding.timePicker.setHour(selectedHour);
                    binding.timePicker.setMinute(selectedMinute);
                } else {
                    binding.timePicker.setCurrentHour(selectedHour);
                    binding.timePicker.setCurrentMinute(selectedMinute);
                }
            });
        }
    }

    /**
     * Apply fixes to TimePicker - maintains fade effect with small dividers
     */
    private void applyTimePickerFixes() {
        try {
            binding.timePicker.postDelayed(() -> {
                adjustNumberPickers(binding.timePicker);
                binding.timePicker.invalidate();
            }, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find and adjust all NumberPickers - DON'T TOUCH FADE SETTINGS
     */
    private void adjustNumberPickers(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof NumberPicker) {
                NumberPicker numberPicker = (NumberPicker) child;

                // ONLY set text properties and small dividers
                // DON'T touch any fade settings - let XML theme handle it
                setNumberPickerText(numberPicker);
                setSmallDividers(numberPicker);

                // Add spacing between hour and minute pickers
                addSpacingBetweenPickers(numberPicker);

            } else if (child instanceof ViewGroup) {
                adjustNumberPickers((ViewGroup) child);
            }
        }
    }

    /**
     * Add more space between hour and minute NumberPickers
     */
    private void addSpacingBetweenPickers(NumberPicker numberPicker) {
        try {
            ViewGroup.LayoutParams params = numberPicker.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;

                // Add smaller horizontal margin to avoid text cutoff
                int spacingPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 8, // Reduced to 8dp spacing
                        context.getResources().getDisplayMetrics());

                marginParams.leftMargin = spacingPx;
                marginParams.rightMargin = spacingPx;

                numberPicker.setLayoutParams(marginParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set text size and styling for NumberPicker
     */
    private void setNumberPickerText(NumberPicker numberPicker) {
        try {
            Field[] fields = NumberPicker.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("mInputText")) {
                    field.setAccessible(true);
                    EditText inputText = (EditText) field.get(numberPicker);
                    if (inputText != null) {
                        inputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                        inputText.setTextColor(ContextCompat.getColor(context, R.color.action_primary));
                        inputText.setGravity(Gravity.CENTER);

                        // Set bold font
                        Typeface typeface = ResourcesCompat.getFont(context, R.font.nunito);
                        inputText.setTypeface(typeface != null ? typeface : Typeface.DEFAULT, Typeface.BOLD);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set small divider height
     */
    private void setSmallDividers(NumberPicker numberPicker) {
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field heightField = NumberPicker.class.getDeclaredField("mSelectionDividerHeight");
            heightField.setAccessible(true);
            heightField.setInt(numberPicker, 6); // Small divider height
        } catch (Exception e) {
            // Ignore if field not available
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