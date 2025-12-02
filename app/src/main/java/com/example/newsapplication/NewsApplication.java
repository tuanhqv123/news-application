package com.example.newsapplication;

import android.app.Application;
import com.squareup.picasso.Picasso;

public class NewsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Disable Picasso debug indicators (blue/green/red corners)
        Picasso picasso = new Picasso.Builder(this)
                .indicatorsEnabled(false)
                .loggingEnabled(false)
                .build();
        Picasso.setSingletonInstance(picasso);
    }
}
