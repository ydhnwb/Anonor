package com.example.android.talktime.ui;

import android.app.Application;

import com.example.android.talktime.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class TalkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/nexa_light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
