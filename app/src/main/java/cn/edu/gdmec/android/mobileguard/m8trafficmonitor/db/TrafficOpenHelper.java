package cn.edu.gdmec.android.mobileguard.m8trafficmonitor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lenovo on 2017/11/26.
 */

public class TrafficOpenHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "traffic.db";
    private static final String TABLE_NAME = "traffic";
    private static final String GPRS = "gprs";
    private static final String TIME = "date";
    public TrafficOpenHelper(Context context) {
        super(context, DB_NAME, null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table" + TABLE_NAME
                + "( id integer primary key autoincremenet," +  GPRS
                + "( varchar( 255 )," + TIME + "datetime)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
