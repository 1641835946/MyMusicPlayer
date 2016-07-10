package com.example.administrator.mymusicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.mymusicplayer.com.example.administrator.mymusicplayer.db.DB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayingActivity extends Activity implements MediaPlayer.OnPreparedListener{

    List<MusicInfo> infoList = new ArrayList<>();
    private DB mDB;
    private ProgressDialog progressDialog;
    private RecyclerView mRecyclerView;
    private Button btn;
    private MusicInfoAdapter mAdapter;
    public static final int UPDATE_TEXT = 1;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        mDB = DB.getInstance(this);
        mRecyclerView = (RecyclerView)findViewById(R.id.music_recycler_view);
        //创建默认的线性LayoutManager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(PlayingActivity.this));
//如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
//创建并设置Adapter
        init();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        btn = (Button) findViewById(R.id.refresh_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(PlayingActivity.this);
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
            }
        });
    }

    private void init() {
        mAdapter = new MusicInfoAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new MusicInfoAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, MusicInfo data) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                }
                long id = data.getId();
                Uri contentUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    //少写了android:name="com.example.administrator.mymusicplayer.MyApplication
                    //所以MyApplication.getContext()一直为null
                    mMediaPlayer.setDataSource(MyApplication.getContext(), contentUri);
                    //少了这句话，又折腾了很久
                    mMediaPlayer.setOnPreparedListener(PlayingActivity.this);
                    mMediaPlayer.prepareAsync();
                    mMediaPlayer.setLooping(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //mMediaPlayer.start();
            }
        });
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
