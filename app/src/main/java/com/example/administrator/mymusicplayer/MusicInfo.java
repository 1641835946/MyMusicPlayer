package com.example.administrator.mymusicplayer;

/**
 * Created by Administrator on 2016/7/10.
 */
public class MusicInfo {

    private String title;

    private long id;

    private int duration;

    private String artist;

    private int size;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public int getSize() {
        return size;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
