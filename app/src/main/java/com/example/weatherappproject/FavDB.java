package com.example.weatherappproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavDB extends SQLiteOpenHelper {

    private static int DB_VERSION = 1;
    private static String DATABASE_NAME = "cities";
    private static String TABLE_NAME = "favoriteTable";
    public static String ITEM_TITLE = "itemTitle";

    private static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + ITEM_TITLE+ " TEXT)";

    public FavDB(Context context){
        super(context,DATABASE_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertIntoTheDatabase(String item_title, int item_image, String id, String fav_status) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ITEM_TITLE, item_title);
        db.insert(TABLE_NAME,null, cv);
    }
    public Cursor read_all_data(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_NAME + " where " + TABLE_NAME +"="+name+"";
        return db.rawQuery(sql,null,null);
    }
    public void remove_fav(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE "+ TABLE_NAME +"="+name+"";
        db.execSQL(sql);

    }
}
