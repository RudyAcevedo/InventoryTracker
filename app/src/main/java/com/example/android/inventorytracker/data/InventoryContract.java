package com.example.android.inventorytracker.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Rudster on 12/1/2016.
 */

public class InventoryContract {
    private InventoryContract() {
    }


    public static final String CONTENT_AUTHORITY = "com.example.android.inventorytracker";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";


    public static abstract class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_CATEGORY = "category";
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_SUPPLIER = "supplier";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
    }
}






