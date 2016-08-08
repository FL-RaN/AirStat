package com.qi.airstat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {
    public DatabaseManager(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(Constants.DATABASE_QUERY_CREATE_AIR_TABLE);
        database.execSQL(Constants.DATABASE_QUERY_CREATE_HEART_RATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(Constants.DATABASE_QUERY_DROP_TABLE_HEART_RATE);
        database.execSQL(Constants.DATABASE_QUERY_DROP_TABLE_AIR);
    }

    public void rebuild() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(Constants.DATABASE_QUERY_DROP_TABLE_HEART_RATE);
        database.execSQL(Constants.DATABASE_QUERY_DROP_TABLE_AIR);
        database.execSQL(Constants.DATABASE_QUERY_CREATE_AIR_TABLE);
        database.execSQL(Constants.DATABASE_QUERY_CREATE_HEART_RATE_TABLE);
        database.close();
    }
}
