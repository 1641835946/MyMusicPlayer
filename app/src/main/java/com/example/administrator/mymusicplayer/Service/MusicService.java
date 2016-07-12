package com.example.administrator.mymusicplayer.Service;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.example.administrator.mymusicplayer.MyApplication;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mMediaPlayer;

    private ControlBinder mBinder = new ControlBinder();

    public class ControlBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (intent != null) {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.release();
                    }

                    long id = intent.getLongExtra("id", 0);
                    Uri contentUri = ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    playMusic(contentUri);
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void pauseMusic() {
        if (mMediaPlayer !=null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void startMusic() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void stopMusic() {
        if (mMediaPlayer != null) mMediaPlayer.stop();
    }

    private void playMusic(Uri contentUri) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
            // could not get audio focus.
        }
        try {
            //少写了android:name="com.example.administrator.mymusicplayer.MyApplication
            //所以MyApplication.getContext()一直为null
            mMediaPlayer.setDataSource(MyApplication.getContext(), contentUri);
            //异步时，少了这句话，又折腾了很久
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
            //用prepare（）不能播放：Should have subtitle controller already set
            mMediaPlayer.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }

    }
}
