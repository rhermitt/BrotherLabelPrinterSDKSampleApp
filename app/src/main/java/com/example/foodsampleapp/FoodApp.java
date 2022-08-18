package com.example.foodsampleapp;

import android.app.Application;
import android.content.Context;

public final class FoodApp extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        FoodApp.appContext = getApplicationContext();
    }

    public static Context getAppContext() {

        return FoodApp.appContext;
    }
}
