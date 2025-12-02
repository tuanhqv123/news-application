package com.example.newsapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class FontSizeManager {

    private static final String PREF_NAME = "FontSizePrefs";
    private static final String KEY_FONT_SIZE = "article_font_size";

    public static final float FONT_SIZE_SMALL = 12f;
    public static final float FONT_SIZE_MEDIUM = 14f;
    public static final float FONT_SIZE_LARGE = 16f;
    public static final float FONT_SIZE_EXTRA_LARGE = 18f;

    private SharedPreferences prefs;

    public FontSizeManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setFontSize(float fontSize) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_FONT_SIZE, fontSize);
        editor.apply();
    }

    public float getFontSize() {
        return prefs.getFloat(KEY_FONT_SIZE, FONT_SIZE_MEDIUM);
    }

    public void increaseFontSize() {
        float currentSize = getFontSize();
        float newSize = currentSize;

        if (currentSize < FONT_SIZE_SMALL) {
            newSize = FONT_SIZE_SMALL;
        } else if (currentSize < FONT_SIZE_MEDIUM) {
            newSize = FONT_SIZE_MEDIUM;
        } else if (currentSize < FONT_SIZE_LARGE) {
            newSize = FONT_SIZE_LARGE;
        } else if (currentSize < FONT_SIZE_EXTRA_LARGE) {
            newSize = FONT_SIZE_EXTRA_LARGE;
        }

        setFontSize(newSize);
    }

    public void decreaseFontSize() {
        float currentSize = getFontSize();
        float newSize = currentSize;

        if (currentSize > FONT_SIZE_EXTRA_LARGE) {
            newSize = FONT_SIZE_EXTRA_LARGE;
        } else if (currentSize > FONT_SIZE_LARGE) {
            newSize = FONT_SIZE_LARGE;
        } else if (currentSize > FONT_SIZE_MEDIUM) {
            newSize = FONT_SIZE_MEDIUM;
        } else if (currentSize > FONT_SIZE_SMALL) {
            newSize = FONT_SIZE_SMALL;
        }

        setFontSize(newSize);
    }

    public String getFontSizeLabel() {
        float size = getFontSize();
        if (size <= FONT_SIZE_SMALL) return "Small";
        if (size <= FONT_SIZE_MEDIUM) return "Medium";
        if (size <= FONT_SIZE_LARGE) return "Large";
        return "Extra Large";
    }

    public void resetToDefault() {
        setFontSize(FONT_SIZE_MEDIUM);
    }
}