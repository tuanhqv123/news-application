package com.example.newsapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// Utility class for date formatting
public class DateUtils {

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    // Format ISO date string to human readable format (e.g., "April, 21")
    public static String formatToMonthDay(String isoDate) {
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
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM, d", Locale.US);
            return outputFormat.format(date);
            
        } catch (Exception e) {
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
            }
            return "";
        }
    }

    // Format ISO date string to short format (e.g., "Apr, 21")
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

    // Format ISO date string to full format (e.g., "Dec 2, 2025")
    public static String formatToFullDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            String dateStr = isoDate.split("\\.")[0];
            if (dateStr.contains("+")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("+"));
            }
            
            Date date = isoFormat.parse(dateStr);
            if (date == null) return "";
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            return outputFormat.format(date);
            
        } catch (Exception e) {
            return "";
        }
    }

    // Get relative time string (e.g., "2 hours ago", "Yesterday")
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
