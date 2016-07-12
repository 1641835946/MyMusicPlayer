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
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.mymusicplayer.MusicInfo;
import com.example.administrator.mymusicplayer.MusicInfoAdapter;
import com.example.administrator.mymusicplayer.R;
import com.example.administrator.mymusicplayer.Service.MusicService;
import com.example.administrator.mymusicplayer.db.DB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    private SeekBar seekBar;
    private MusicInfoAdapter mAdapter;
    public static final int UPDATE_TEXT = 1;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private IntentFilter intentFilter;
    private ControlReceiver controlReceiver;
    private MusicService musicService;
    private boolean mBound = false;
    private boolean playing = false;
    private List<MusicInfo> list;
    private List<String> listOnlyTitle;
    private int whichPlay;
    private String which;
    private int maxNum;

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
            MusicService.ControlBinder binder = (MusicService.ControlBinder) service;
            musicService = binder.getService();
            mBound = true;
            if (musicService !=null && !musicService.isPlaying()) {
                if (!readData()) {
                    musicService.playFirstMusic();
                    playMusic(0);
                }
                Log.e("梁洁", "111111111");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private boolean readData() {
        boolean re = false;
        FileInputStream in = null;
        BufferedReader reader = null;
        MusicInfo musicInfo = new MusicInfo();
        try {
            in = openFileInput("playHistory");
            reader = new BufferedReader(new InputStreamReader(in));
            musicInfo.setTitle(reader.readLine());
            musicInfo.setId(Long.parseLong(reader.readLine()));
            for (int i = 0; i<list.size(); i++) {
                if (list.get(i).getTitle().equals(musicInfo.getTitle()))
                    if (list.get(i).getId() == (musicInfo.getId()))
                        whichPlay = i;
            }
            musicService.playFirstMusic();
            playMusic(whichPlay);
            re = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return re;
    }


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
        seekBar = (SeekBar) findViewById(R.id.seek_bar);

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.AUDIO_BECOMING_NOISY");
        controlReceiver = new ControlReceiver();
        registerReceiver(controlReceiver, intentFilter);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_pause:
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                    playing = false;
                }
                else {
                    musicService.startMusic();
                    playing = true;
                }
                break;
            case R.id.last:
                if (!musicService.isPlaying()) playing = true;
                if (whichPlay == 0) {
                    whichPlay = maxNum-1;
                    playMusic(whichPlay);
                } else playMusic(--whichPlay);
                break;
            case R.id.next:
                if (!playing) playing = true;
                if (whichPlay == (maxNum-1)) {
                    whichPlay = 0;
                    playMusic(whichPlay);
                } else playMusic(++whichPlay);
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

//1.双击暂停问题
    private void init() {
        list = mDB.loadLocalMusicInfo();
        listOnlyTitle = mDB.loadOnlyTitle();
        maxNum = list.size();
        mAdapter = new MusicInfoAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new MusicInfoAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, MusicInfo data) {
                if (!playing) playing = true;
                which = data.getTitle();
                whichPlay = listOnlyTitle.indexOf(which);
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
        musicName.setText(list.get(id).getTitle());
        startService(intent);
    }

    private void stopMusicService() {
        Intent intent = new Intent(ListActivity.this, MusicService.class);
        stopService(intent);
        playing = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (!playing) {
            Intent intent = new Intent(this, MusicService.class);
            stopService(intent);
        }
        unregisterReceiver(controlReceiver);
        Log.e("梁洁", "whichPlay is " + whichPlay);
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("playHistory", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(list.get(whichPlay).getTitle());
            writer.newLine();
            writer.write(String.valueOf(list.get(whichPlay).getId()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            musicService.pauseMusic();
        }
    }

}
