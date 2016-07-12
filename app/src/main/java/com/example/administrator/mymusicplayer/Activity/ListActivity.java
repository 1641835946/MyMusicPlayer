package com.example.administrator.mymusicplayer.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.mymusicplayer.MusicInfo;
import com.example.administrator.mymusicplayer.MusicInfoAdapter;
import com.example.administrator.mymusicplayer.R;
import com.example.administrator.mymusicplayer.Service.MusicService;
import com.example.administrator.mymusicplayer.Service.MyIntentService;
import com.example.administrator.mymusicplayer.Service.MyService;
import com.example.administrator.mymusicplayer.db.DB;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends Activity implements View.OnClickListener {

    List<MusicInfo> infoList = new ArrayList<>();
    private DB mDB;
    private ProgressDialog progressDialog;
    private RecyclerView mRecyclerView;
    private Button btn;
    private Button startbtn;
    private Button lastbtn;
    private Button nextbtn;
    private TextView musicName;
    private TextView totalTime;
    private MusicInfoAdapter mAdapter;
    public static final int UPDATE_TEXT = 1;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private IntentFilter intentFilter;
    private ControlReceiver controlReceiver;
    private MusicService musicService;
    private boolean mBound = false;
    private boolean playing = false;
    private List<MusicInfo> list;
    private int whichPlay;
    private String which;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    progressDialog.dismiss();
                    init();
                    break;
                default:
                    break;
            }
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.ControlBinder binder = (MusicService.ControlBinder) service;
            musicService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mDB = DB.getInstance(this);
        mRecyclerView = (RecyclerView)findViewById(R.id.music_recycler_view);
        //创建默认的线性LayoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ListActivity.this));
//如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
//创建并设置Adapter

        init();

//        Intent intent = new Intent(ListActivity.this, MusicService.class);
//        intent.putExtra("id", list.get(0).getId());
//        intent.putExtra("title", list.get(0).getTitle());
//        startService(intent);
//        musicService.pauseMusic();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        btn = (Button) findViewById(R.id.refresh_btn);
        btn.setOnClickListener(this);
        startbtn = (Button) findViewById(R.id.start_pause);
        startbtn.setOnClickListener(this);
        lastbtn = (Button) findViewById(R.id.last);
        lastbtn.setOnClickListener(this);
        nextbtn = (Button) findViewById(R.id.next);
        nextbtn.setOnClickListener(this);
        musicName = (TextView) findViewById(R.id.music_name);
        totalTime = (TextView) findViewById(R.id.music_total_time);

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.AUDIO_BECOMING_NOISY");
        controlReceiver = new ControlReceiver();
        registerReceiver(controlReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_pause:
                if (playing) {
                    musicService.pauseMusic();
                    playing = false;
                }
                else {
                    musicService.startMusic();
                    playing = true;
                }
                break;
            case R.id.last:
                Intent liangjie = new Intent(this, MyService.class);
                startService(liangjie);
                Intent jjjjj= new Intent(this, MyIntentService.class);
                startService(jjjjj);
                --whichPlay;
                playMusic(whichPlay);
                break;
            case R.id.next:
                Intent jieliangjie = new Intent(this, MyService.class);
                stopService(jieliangjie);
                Intent iiii= new Intent(this, MyIntentService.class);
                stopService(iiii);
                ++whichPlay;
                playMusic(whichPlay);
                break;
            case R.id.refresh_btn:
                progressDialog = new ProgressDialog(ListActivity.this);
                progressDialog.setTitle("正在搜索，请稍等");
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mDB.loadLocalMusicInfo().isEmpty()) {
                            mDB.clearLocalMusicInfo();
                        }
                        ContentResolver contentResolver = getContentResolver();
                        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        Cursor cursor = contentResolver.query(uri, null, null, null, null);
                        if (cursor == null) {
                            // query failed, handle error.
                        } else if (!cursor.moveToFirst()) {
                            // no media on the device
                        } else {
                            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
                            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
                            do {
                                MusicInfo info = new MusicInfo();
                                info.setId(cursor.getLong(idColumn));
                                info.setTitle(cursor.getString(titleColumn));
                                infoList.add(info);
                            } while (cursor.moveToNext());
                        }
                        mDB.saveLocalMusicInfo(infoList);
                        Message message = new Message();
                        message.what = UPDATE_TEXT;
                        handler.sendMessage(message);
                    }
                }).start();
                break;
        }
    }


    private void init() {
        list = mDB.loadLocalMusicInfo();
        mAdapter = new MusicInfoAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new MusicInfoAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, MusicInfo data) {
                playing = true;
                which = data.getTitle();
                whichPlay = list.indexOf(data);
                musicName.setText(which);
                totalTime.setText("time");
                musicService.stopMusic();
                stopMusicService();
                Intent intent = new Intent(ListActivity.this, MusicService.class);
                intent.putExtra("id", data.getId());
                intent.putExtra("title", which);
                startService(intent);
            }
        });
    }

    private void playMusic(int id) {
        Intent intent = new Intent(ListActivity.this, MusicService.class);
        intent.putExtra("id", list.get(id).getId());
        intent.putExtra("title", list.get(id).getTitle());
        startService(intent);
    }

    private void stopMusicService() {
        Intent intent = new Intent(ListActivity.this, MusicService.class);
        stopService(intent);
        playing = false;
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        unregisterReceiver(controlReceiver);
    }

    class ControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            musicService.pauseMusic();
        }
    }

}
