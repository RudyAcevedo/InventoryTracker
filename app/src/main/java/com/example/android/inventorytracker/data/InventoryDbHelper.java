package com.example.android.inventorytracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventorytracker.data.InventoryContract.InventoryEntry;
/**
 * Created by Rudster on 12/1/2016.
 */

public class InventoryDbHelper extends SQLiteOpenHelper{

    // database version
    public static final int DATABASE_VERSION = 1;

    //name of database file
    public static final String DATABASE_NAME = "inventory.db";


    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS" + InventoryContract.InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //CREATE TABLE inventory
        String SQL_CREATE_TABLE =
                "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + "("
                        + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + InventoryEntry.COLUMN_ITEM_CATEGORY + " INTEGER NOT NULL,"
                        + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL,"
                        + InventoryEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL,"
                        + InventoryEntry.COLUMN_ITEM_SUPPLIER + " TEXT NOT NULL,"
                        + InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL);";
        db.execSQL(SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //This database is only a cache for online data, so its upgrade policy is simply
        //to discard the data and start over
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);

    }


}
