package com.example.machinenote.Utility;

public class HandleQRCode {
    public static String getLinijaSapFromQR(String qr) {
        if (qr == null || qr.length() <= 3) {
            // Handle the case where the QR code string is null or too short
            return "";
        }
        // Return the substring starting from the fourth character (index 3)
        return qr.substring(3);
    }

    public static int getRezervniDeliIdFromQR(String qr) {
        if (qr == null || qr.length() <= 6) {
            // Handle the case where the QR code string is null or too short
            return 0;
        }
        // Return the substring starting from the fourth character (index 3)
        try {
            return Integer.parseInt(qr.substring(2, 7));
        } catch (Exception e) {
            return 0;
        }
    }
}
