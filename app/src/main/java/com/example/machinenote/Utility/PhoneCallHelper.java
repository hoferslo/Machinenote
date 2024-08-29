package com.example.machinenote.Utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PhoneCallHelper {

    private final Context context;

    public PhoneCallHelper(Context context) {
        this.context = context;
    }

    // Prepare the call and return an Intent
    public Intent prepareDialer(String phoneNumber) {
        // Create the dial Intent
        return createDialIntent(phoneNumber);
    }

    // Create the dial Intent
    private Intent createDialIntent(String phoneNumber) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        return dialIntent;
    }
}
