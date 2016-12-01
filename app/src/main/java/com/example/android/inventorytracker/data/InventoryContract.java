package com.example.android.inventorytracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Rudster on 12/1/2016.
 */

public class InventoryContract {
    private InventoryContract(){
    }


    public static final String CONTENT_AUTHORITY = "com.example.android.inventorytracker";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";


    public static abstract class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_CATEGORY = "category";
        public static final String COLUMN_ITEM_NAME= "name";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_SUPPLIER = "supplier";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";

        /**
         * possible values for item category.
         */

        public static final int CATEGORY_UNKNOWN = 0;
        public static final int CATEGORY_BEVERAGE = 1;
        public static final int  CATEGORY_CHIPS= 2;
        public static final int CATEGORY_CANDY = 3;

        /**
         * Returns whether or not the given gender is CATEGORY_BEVERAGE, CATEGORY_CHIPS, OR
         * CATEGORY_CANDY
         */
        public static boolean isValidCategory(int category) {
            if (category == CATEGORY_UNKNOWN || category == CATEGORY_BEVERAGE
                    || category == CATEGORY_CHIPS || category == CATEGORY_CANDY) {
                return true;
            }
            return false;
        }
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
    }




}
