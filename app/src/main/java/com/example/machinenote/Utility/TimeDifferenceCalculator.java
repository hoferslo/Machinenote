package com.example.machinenote.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeDifferenceCalculator {
    public static String pattern = "yyyy-MM-dd HH:mm:ss";

    public static int getMinutesBetweenDates(String startDatestr, String endDatestr) throws ParseException {
        // Get the difference in milliseconds
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date startDate = dateFormat.parse(startDatestr);
        Date endDate = dateFormat.parse(endDatestr);
        long differenceInMillis = endDate.getTime() - startDate.getTime();

        // Convert milliseconds to minutes
        return (int) TimeUnit.MILLISECONDS.toMinutes(differenceInMillis);
    }

    public static int getYearFromDateString(String dateString) {
        // Define the format of the input date string
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        try {
            // Parse the date string to a Date object
            Date date = dateFormat.parse(dateString);

            // Use Calendar to extract the year
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Return a default value in case of error
        }
    }

    public static int getMonthFromDateString(String dateString) {
        // Define the format of the input date string
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        try {
            // Parse the date string to a Date object
            Date date = dateFormat.parse(dateString);

            // Use Calendar to extract the month (Note: Calendar.MONTH is 0-based)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Add 1 to month to convert from 0-based index (January = 0) to 1-based month number
            return calendar.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Return a default value in case of error
        }
    }
}
