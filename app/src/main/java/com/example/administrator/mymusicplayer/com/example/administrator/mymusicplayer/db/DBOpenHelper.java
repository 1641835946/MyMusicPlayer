package com.example.administrator.mymusicplayer.com.example.administrator.mymusicplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/7/10.
 */
public class DBOpenHelper extends SQLiteOpenHelper{

    public static final String CREATE_LOCALMUSIC = "create table LocalMusic (" +
            "id integer primary key autoincrement, " +
            "music_title text, " +
            "music_id text)";

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCALMUSIC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
