package com.example.administrator.mymusicplayer.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    public int liangjie = 1994;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        liangjie++;
        Log.e("梁洁", "MyService onCreate" + liangjie);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        liangjie++;
        Log.e("梁洁", "MyService onStartCommand" + liangjie);
        Log.e("梁洁", "MyService onStartCommand thread id is " + Thread.currentThread().getId());
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("梁洁", "onDestroy");
    }
}
