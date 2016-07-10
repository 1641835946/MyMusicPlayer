package com.example.administrator.mymusicplayer;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2016/7/10.
 */
public class MyApplication extends Application {

    private static Context context ;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
