package com.example.administrator.mymusicplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.mymusicplayer.MusicInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/10.
 */
public class DB {

    public static final String DB_NAME = "my_music_player";
    public static final int VERSION = 1;
    private static DB mDB;
    private SQLiteDatabase db;

    private DB(Context context) {
        DBOpenHelper dbHelper = new DBOpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public synchronized static DB getInstance(Context context) {
        if (mDB == null) {
            mDB = new DB(context);
        }
        return mDB;
    }

    public void saveLocalMusicInfo(List<MusicInfo> infoList) {
        if (infoList != null) {
            ContentValues values = new ContentValues();
            for (int i = 0; i < infoList.size(); i++) {
                values.clear();
                values.put("music_id", infoList.get(i).getId());
                values.put("music_title", infoList.get(i).getTitle());
                db.insert("LocalMusic", null, values);
            }
        }
    }

    public List<MusicInfo> loadLocalMusicInfo() {
        List<MusicInfo> list = new ArrayList<>();
        Cursor cursor = db.query("LocalMusic", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                MusicInfo aMusic = new MusicInfo();
                aMusic.setId(cursor.getInt(cursor.getColumnIndex("music_id")));
                aMusic.setTitle(cursor.getString(cursor.getColumnIndex("music_title")));
                list.add(aMusic);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public void clearLocalMusicInfo() {
        db.delete("LocalMusic", "id >= ?", new String[]{"0"});
    }
}
