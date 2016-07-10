package com.example.administrator.mymusicplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ListActivity extends Activity implements View.OnClickListener{

    private Button btnStart;
    private Button btnPause;
    private Button btnStop;
    private View view;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnPause = (Button) findViewById(R.id.btn_pause);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        new Thread(new Runnable() {       // service
            @Override
            public void run() {
                mediaPlayer = MediaPlayer.create(ListActivity.this, R.raw.music);
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                break;
            case R.id.btn_pause:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;
            case R.id.btn_stop:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            //mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
