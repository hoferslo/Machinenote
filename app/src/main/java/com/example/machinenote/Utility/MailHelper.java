package com.example.machinenote.Utility;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MailHelper {

    /**
     * Opens the email client with recipient, subject, and body pre-filled.
     *
     * @param context   The context from which this method is called.
     * @param recipient The recipient's email address.
     * @param subject   The subject of the email.
     * @param body      The body of the email.
     */
    public static void openEmailClient(Context context, String recipient, String subject, String body) {
        try {
            // Create an intent to open the email client
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822"); // Ensure that only email apps can handle this

            // Add the recipient, subject, and body to the intent
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            // Start the email client activity
            context.startActivity(Intent.createChooser(emailIntent, "Choose an email client:"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error opening email client.", Toast.LENGTH_SHORT).show();
        }
    }
}
