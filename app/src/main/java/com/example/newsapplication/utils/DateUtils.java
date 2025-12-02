package com.example.newsapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for date formatting
 */
public class DateUtils {

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    /**
     * Format ISO date string to human readable format (e.g., "April, 21")
     * @param isoDate ISO date string like "2025-09-25T01:00:00+00:00"
     * @return Formatted date string like "September, 25"
     */
    public static String formatToMonthDay(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        
        try {
            // Parse ISO date
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            // Handle timezone offset
            String dateStr = isoDate;
            if (dateStr.contains("+")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("+"));
            } else if (dateStr.lastIndexOf("-") > 10) {
                dateStr = dateStr.substring(0, dateStr.lastIndexOf("-"));
            }
            
            Date date = isoFormat.parse(dateStr);
            if (date == null) return "";
            
            // Format to "Month, day"
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM, d", Locale.US);
            return outputFormat.format(date);
            
        } catch (Exception e) {
            // Fallback: try to extract month and day manually
            try {
                String[] parts = isoDate.split("T")[0].split("-");
                if (parts.length >= 3) {
                    int month = Integer.parseInt(parts[1]) - 1;
                    int day = Integer.parseInt(parts[2]);
                    if (month >= 0 && month < 12) {
                        return MONTH_NAMES[month] + ", " + day;
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
            return "";
        }
    }

    /**
     * Format ISO date string to short format (e.g., "Apr, 21")
     * @param isoDate ISO date string
     * @return Short formatted date string
     */
    public static String formatToShortMonthDay(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            String dateStr = isoDate;
            if (dateStr.contains("+")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("+"));
            } else if (dateStr.lastIndexOf("-") > 10) {
                dateStr = dateStr.substring(0, dateStr.lastIndexOf("-"));
            }
            
            Date date = isoFormat.parse(dateStr);
            if (date == null) return "";
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM, d", Locale.US);
            return outputFormat.format(date);
            
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "Yesterday")
     * @param isoDate ISO date string
     * @return Relative time string
     */
    public static String getRelativeTime(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            String dateStr = isoDate;
            if (dateStr.contains("+")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("+"));
            } else if (dateStr.lastIndexOf("-") > 10) {
                dateStr = dateStr.substring(0, dateStr.lastIndexOf("-"));
            }
            
            Date date = isoFormat.parse(dateStr);
            if (date == null) return "";
            
            long diffMs = System.currentTimeMillis() - date.getTime();
            long diffMinutes = diffMs / (60 * 1000);
            long diffHours = diffMs / (60 * 60 * 1000);
            long diffDays = diffMs / (24 * 60 * 60 * 1000);
            
            if (diffMinutes < 1) {
                return "Just now";
            } else if (diffMinutes < 60) {
                return diffMinutes + " min ago";
            } else if (diffHours < 24) {
                return diffHours + " hour" + (diffHours > 1 ? "s" : "") + " ago";
            } else if (diffDays < 7) {
                return diffDays + " day" + (diffDays > 1 ? "s" : "") + " ago";
            } else {
                return formatToShortMonthDay(isoDate);
            }
            
        } catch (Exception e) {
            return "";
        }
    }
}
